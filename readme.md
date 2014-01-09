ObjPackager is a companion to [GLView](https://github.com/wartman4404/android-glview) which parses [OBJ files]([https://en.wikipedia.org/wiki/Wavefront_.obj_file) into the binary format that it prefers.  These consist mainly of ready-to-use vertex data, offering a noticeable speed improvement on Android.

ObjPackager is built around Sean Owen's [oObjLoader](https://github.com/seanrowens/oObjLoader), and shares its limitations while adding a few of its own.  Only OBJ groups are supported, individual pieces composing an object are expected to be named `CommonGroupName_DistinctName` and should each have a single material, triangles are the only shape supported, normals must be present.  A file meeting these limitations can be produced in Blender by checking "Objects as OBJ Groups", "Triangulate Faces", "Include Normals", and "Material Groups" and unchecking "Objects as OBJ Objects".

### Usage

`java -cp com.github.wartman4404.objpackager.Main input.obj` will produce input.[jbov](#"so named because it's the same obj file, just mangled and flipped around") (vertex data) and input.jbom (material data)
