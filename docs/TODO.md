# TODO

## Base doclet generalization

Current Jada implementation is focused on the standard doclet shipping with the JDK (`jdk.javadoc.doclet.StandardDoclet`), by far the most popular. In case further doclets become relevant besides the standard one, it will be necessary to decouple the logic specific to the latter defining a `BaseDocletAdapter` whose implementations will be activated via SPI to allow third parties to define their own adapters.