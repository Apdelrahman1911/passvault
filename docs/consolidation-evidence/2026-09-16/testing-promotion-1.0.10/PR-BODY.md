Promote the exact verified main snapshot **e02e2243793062974f93766ba44183480fb92f99**, tree `71e36517fbf54ff1ed36546378a43a7cfea4cbe2`, to testing.

- Includes reviewed PR191 Intel compiler verification/native-link correction and PR192 owner-approved **1.0.10 / Store1017004** metadata.
- No extra changes beyond main; preserves identities, dependencies and all deferred issues/restrictions.
- PR192 CI35042932278 passed all12jobs, including Intel packaging and unsigned optimized iOS linking. Main post-merge CI must pass before opening this PR.
- This PR and its CI do not upload. Normal protected approval, final testing-source checks and fresh release admission precede the testing-only Store workflow. Legacy push switch remains OFF. No production or reuse of1017001/2/3.
