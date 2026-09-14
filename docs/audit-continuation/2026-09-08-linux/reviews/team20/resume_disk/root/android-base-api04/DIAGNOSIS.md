# Root source interpretation — pending independent challenge

Exact BaseR8Task.class9846B/SHA256ed01e6f9da56a03deed2dc7145c40796907eac626e67c3e07dd74c619866e958.
Metadata declares getProgramClasses and both CombinedMainDex helpers with
flags0x0014 (protected final), not public. Class.getMethod on the R8 superclass
chain therefore cannot satisfy the harness's public capability check. The
Android03 observed NoSuchMethodException is consistent with this declaration.
The program helper is actively invoked later by the harness, so simply deleting
the guard would not supply a supported public call. No such change was made.

Root independently read the retained Code/constant pool as binary DATA, never
imported/executed vendor code. getProgramClasses has this complete code hex:
2ab60094b900970100c00099b6009c99002c2ab6009fb900970100c00099b6009c9a001a2ab600a1b9006a0100c0006cb900700100b800a5a7000d2ab600a8c00081b80087b0

CP148 -> getShrinkingWithDynamicFeatures:Property; CP159 ->
getHasAllAccessTransformers:Property; Property.get/Boolean.booleanValue form the
short-circuited condition. If shrinkingWithDynamicFeatures is true and
hasAllAccessTransformers is false, return listOf(baseJar.get().asFile);
otherwise return classes.toList(). CP161/getBaseJar:RegularFileProperty,
CP168/getClasses:ConfigurableFileCollection. This is a candidate exact public
property reconstruction, not yet a patch: the inherited property declarations
still need evidence from the named ProguardConfigurableTask superclass.

The two protected CombinedMainDex checks are not used by producerInputs; their
unnecessary public-capability requirements are also incompatible. Removing
unused checks alone must not weaken any genuine observed-path/output/input or
resource/ownership constraint. Keep-rule helper declarations need the same
superclass proof before another producer run. No product PVA finding, test pass,
closure, protected reflection, dependency change or automatic retry follows.
