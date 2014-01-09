package com.github.wartman4404.objpackager.gl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.github.wartman4404.objpackager.material.DiffuseTexturedMaterialFactory;
import com.github.wartman4404.objpackager.material.DiffuseTexturedMaterialFactory.DiffuseTexturedMaterialSave;
import com.github.wartman4404.objpackager.material.NormalTexturedMaterialFactory;
import com.github.wartman4404.objpackager.material.NormalTexturedMaterialFactory.NormalTexturedMaterialSave;
import com.github.wartman4404.objpackager.material.UniformColorMaterialFactory;
import com.github.wartman4404.objpackager.material.UniformColorMaterialFactory.UniformMaterialSave;


public class ObjSaver {
	public static final int MATERIAL_DIFFUSE_TEXTURED_ID = 0;
	public static final int MATERIAL_UNIFORM_ID = 1;
	public static final int MATERIAL_NORMAL_TEXTURED_ID = 2;
	public static final int MATERIALCOUNT = 3;
	
	public static GLMaterialSave loadMaterial(int id, DataInputStream in) throws IOException {
		switch (id) {
		case MATERIAL_DIFFUSE_TEXTURED_ID:
			return new DiffuseTexturedMaterialSave(in);
		case MATERIAL_UNIFORM_ID:
			return new UniformMaterialSave(in);
		case MATERIAL_NORMAL_TEXTURED_ID:
			return new NormalTexturedMaterialSave(in);
		default:
			return null;
		}
	}
	
	public static GLMaterial getFactory(int id) {
		switch (id) {
		case MATERIAL_DIFFUSE_TEXTURED_ID:
			return new DiffuseTexturedMaterialFactory();
		case MATERIAL_UNIFORM_ID:
			return new UniformColorMaterialFactory();
		case MATERIAL_NORMAL_TEXTURED_ID:
			return new NormalTexturedMaterialFactory();
		default:
			return null;
		}
	}
	
	public static interface MaterialFactories {
		public GLMaterial getMaterial(int id);
		public GLMaterial[] getLoadedMaterials();
	}
	
	public static class DefaultMaterialFactories implements MaterialFactories {
		protected GLMaterial[] materials = new GLMaterial[MATERIALCOUNT];
		public GLMaterial getMaterial(int id) {
			if (materials[id] == null) {
				materials[id] = getFactory(id);
			}
			return materials[id];
		}
		public GLMaterial[] getLoadedMaterials() {
			int materialCount = 0;
			GLMaterial[] loadedMaterials;
			for (GLMaterial m: materials) {
				if (m != null) materialCount++;
			}
			loadedMaterials = new GLMaterial[materialCount];
			for (GLMaterial m: materials) {
				if (m != null) {
					loadedMaterials[--materialCount] = m;
				}
			}
			return loadedMaterials;
		}
	}

	public static class VBOData {
		public VBOData(Map<String, GLElementGroup> elements, GLMaterial[] materials, ByteBuffer vertBuf, ByteBuffer idxBuf) {
			this.elements = elements;
			this.materials = materials;
			this.vertBuf = vertBuf;
			this.idxBuf = idxBuf;
		} public final Map<String, GLElementGroup> elements;
		public final GLMaterial[] materials;
		public final ByteBuffer vertBuf;
		public final ByteBuffer idxBuf;
	}
	
	private ObjSaver() { }
	
	public static void saveObject(Map<String, GLElementGroup> elements, ByteBuffer vertBuffer, ByteBuffer idxBuffer, DataOutputStream materialOut, DataOutputStream elementOut) throws IOException {
		Map<String, Integer> materialIndices = saveMaterials(getMaterials(elements), materialOut);
		saveElements(elements, materialIndices, elementOut);
		saveBuffers(vertBuffer, idxBuffer, elementOut);
	}
	
	public static void saveBuffers(ByteBuffer vertBuffer, ByteBuffer idxBuffer, DataOutputStream out) throws IOException {
		writeBuffer(vertBuffer, out);
		writeBuffer(idxBuffer, out);
	}
	
	public static void writeBuffer(ByteBuffer buffer, DataOutputStream out) throws IOException {
		out.writeInt(buffer.limit());
		while(buffer.hasRemaining()) {
			out.write(buffer.get());
		}
	}
	
	public static ByteBuffer readBuffer(DataInputStream in) throws IOException {
		int length = in.readInt();
		byte[] data = new byte[length];
		in.read(data);
		ByteBuffer buf = ByteBuffer.allocate(length);
		buf.put(data);
		return buf;
	}
	
	private static GLMaterialSave[] loadMaterials(DataInputStream in) throws IOException {
		int count = in.readInt();
		System.err.println("loadObject: got " + count + " materials");
		GLMaterialSave[] materials = new GLMaterialSave[count];
		for (int i = 0; i < count; i++) {
			int id = in.readByte();
			System.err.println(String.format("material %d: type %d", i, id));
			GLMaterialSave save = loadMaterial(id, in);
			materials[i] = save;
		}
		return materials;
	}
	
    public static VBOData loadFile(InputStream materialIn, InputStream elementIn, MaterialFactories factories) throws IOException {
    	DataInputStream materialData = new DataInputStream(materialIn);
    	DataInputStream elementData = new DataInputStream(elementIn);
    	Map<String, GLElementGroup> elements = ObjSaver.loadObject(materialData, elementData, factories);
    	ByteBuffer vertBuf = ObjSaver.readBuffer(elementData);
    	ByteBuffer idxBuf = ObjSaver.readBuffer(elementData);
    	vertBuf.flip();
    	idxBuf.flip();
    	
    	return new VBOData(elements, factories.getLoadedMaterials(), vertBuf, idxBuf);
    	
    }
	
	public static Map<String, GLElementGroup> loadObject(DataInputStream materialIn, DataInputStream elementIn, MaterialFactories factories) throws IOException {
		GLMaterialSave[] materials = loadMaterials(materialIn);
		return loadElements(elementIn, materials, factories);
	}

	private static Map<String, GLElementGroup> loadElements(DataInputStream in, GLMaterialSave[] materials, MaterialFactories factories) throws IOException {
		Map<String, GLElementGroup> groups = new HashMap<String, GLElementGroup>();
		int count = in.readInt();
		System.err.println("loadObject: got " + count + " element groups");

		for (int i = 0; i < count; i++) {
			String name = readString(in);
			BoundingBox bbox = new BoundingBox(in);
			int groupSize = in.readInt();
			GLElement elements[] = new GLElement[groupSize];
			System.err.println(String.format("Element group \"%s\" with %d elements and bounding box %s", name, groupSize, bbox));
			for (int j = 0; j < groupSize; j++) {
				System.err.print("Element " + j + ": ");
				elements[j] = loadElement(in, materials, factories);
			}
			groups.put(name, new GLElementGroup(elements, bbox));
		}
		return groups;
	}
	
	private static GLElement loadElement(DataInputStream in, GLMaterialSave[] materials, MaterialFactories factories) throws IOException {
		int materialIndex = in.readInt();
		GLMaterialSave save = materials[materialIndex];
		int indexCount = in.readInt();
		int indexOffset = in.readInt();
		int vertexOffset = in.readInt();
		System.err.println(String.format("material index %d, %d indexes starting at %d, vertices starting at %d", materialIndex, indexCount, indexOffset, vertexOffset));
		Shape shape = new Shape(indexOffset, indexCount);
		GLMaterial factory = factories.getMaterial(save.getId());
		GLElement element = new GLElement(shape, factory, save, vertexOffset, null);
		return element;
	}
	
	private static Map<String, GLMaterialSave> getMaterials(Map<String, GLElementGroup> elements) {
		Map<String, GLMaterialSave> materials = new HashMap<String, GLMaterialSave>();
		for (String s: elements.keySet()) {
			for (GLElement elem: elements.get(s).getElements()) {
				GLMaterialSave m = elem.getMaterial();
				materials.put(m.getName(), m);
			}
		}
		return materials;
	}
	
	private static Map<String, Integer> saveMaterials(Map<String, GLMaterialSave> materials, DataOutputStream out) throws IOException {
		out.writeInt(materials.size());
		Map<String, Integer> materialIndices = new HashMap<String, Integer>();
		int idx = 0;
		String[] materialNames = new String[materials.size()];
		materials.keySet().toArray(materialNames);
		Arrays.sort(materialNames);
		for (String name: materialNames) {
			GLMaterialSave material = materials.get(name);
			materialIndices.put(name, idx);
			material.save(out);
			idx++;
		}
		return materialIndices;
	}
	
	private static void saveElements(Map<String, GLElementGroup> elements, Map<String, Integer> materials, DataOutputStream out) throws IOException {
		out.writeInt(elements.size());
		for (String name: elements.keySet()) {
			saveString(out, name);
			GLElementGroup group = elements.get(name);
			group.bbox.save(out);
			out.writeInt(group.elements.length);
			for (GLElement element: group.elements) {
				saveElement(element, materials, out);
			}
		}
	}
	
	private static void saveElement(GLElement element, Map<String, Integer> materials, DataOutputStream out) throws IOException {
		out.writeInt(materials.get(element.getMaterial().getName()));
		out.writeInt(element.getIndexCount());
		out.writeInt(element.getIndexOffset());
		out.writeInt(element.getVertexOffset());
	}
	
	public static void saveString(DataOutputStream out, String string) throws IOException {
		out.writeInt(string.length());
		out.writeBytes(string);
	}
	
	public static String readString(DataInputStream in) throws IOException {
		int length = in.readInt();
		System.err.print("" + length + " byte string: ");
		byte[] bytes = new byte[length];
		in.read(bytes);
		String string = new String(bytes, "ascii");
		System.err.println("\"" + string + "\"");
		return string;
	}

}
