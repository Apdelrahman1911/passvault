#define _POSIX_C_SOURCE 200809L

#include <errno.h>
#include <pthread.h>
#include <signal.h>
#include <stdlib.h>
#include <unistd.h>

/* Linux/glibc-only synthetic child. No files, descendants, or dynamic input. */
static pthread_t leader;
static int joined_cookie;

static void emit(const char *text, size_t length)
{
    while (length != 0) {
        ssize_t count = write(STDOUT_FILENO, text, length);
        if (count < 0 && errno == EINTR) {
            continue;
        }
        if (count <= 0) {
            _exit(71);
        }
        text += (size_t)count;
        length -= (size_t)count;
    }
}

static void command(char expected)
{
    char actual = '\0';
    ssize_t count;
    do {
        count = read(STDIN_FILENO, &actual, 1);
    } while (count < 0 && errno == EINTR);
    if (count != 1 || actual != expected) {
        _exit(72);
    }
}

static void *worker(void *unused)
{
    void *result = NULL;
    (void)unused;
    if (pthread_join(leader, &result) != 0 || result != &joined_cookie) {
        _exit(73);
    }
    emit("LEADER_JOINED\n", sizeof("LEADER_JOINED\n") - 1);
    command('P');
    emit("WORKER_ALIVE\n", sizeof("WORKER_ALIVE\n") - 1);
    command('X');
    return NULL;
}

int main(void)
{
    struct sigaction timeout_action = {0};
    sigset_t timeout_mask;
    pthread_t survivor;

    /* A parent-side pidfd failure must not leave a permanent child. */
    timeout_action.sa_handler = SIG_DFL;
    if (sigemptyset(&timeout_action.sa_mask) != 0 ||
        sigaction(SIGALRM, &timeout_action, NULL) != 0 ||
        sigemptyset(&timeout_mask) != 0 ||
        sigaddset(&timeout_mask, SIGALRM) != 0 ||
        sigprocmask(SIG_UNBLOCK, &timeout_mask, NULL) != 0) {
        return 74;
    }
    (void)alarm(10);

    leader = pthread_self();
    if (pthread_create(&survivor, NULL, worker, NULL) != 0) {
        return 75;
    }
    emit("READY\n", sizeof("READY\n") - 1);
    command('L');
    /* Static storage remains valid after this initial joinable thread exits. */
    pthread_exit(&joined_cookie);
}
