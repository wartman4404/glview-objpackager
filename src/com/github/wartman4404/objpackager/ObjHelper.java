package com.github.wartman4404.objpackager;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.wartman4404.objpackager.gl.BoundingBox;
import com.github.wartman4404.objpackager.gl.GLElement;
import com.github.wartman4404.objpackager.gl.GLElementGroup;
import com.github.wartman4404.objpackager.gl.GLMaterial;
import com.github.wartman4404.objpackager.gl.ObjSaver;
import com.github.wartman4404.objpackager.gl.ObjSaver.MaterialFactories;
import com.github.wartman4404.objpackager.gl.Shape;
import com.github.wartman4404.objpackager.material.DiffuseTexturedMaterialFactory;
import com.github.wartman4404.objpackager.material.UniformColorMaterialFactory;
import com.owens.oobjloader.builder.Build;
import com.owens.oobjloader.builder.Face;
import com.owens.oobjloader.builder.FaceVertex;
import com.owens.oobjloader.builder.Material;
import com.owens.oobjloader.builder.VertexGeometric;
import com.owens.oobjloader.builder.VertexNormal;
import com.owens.oobjloader.builder.VertexTangent;
import com.owens.oobjloader.builder.VertexTexture;
import com.owens.oobjloader.parser.MaterialLoader;

public class ObjHelper {
	private ObjHelper() { }
	
	public static Map<String, GLElementGroup> loadElements(Build build, ByteBuffer vertBuf, ByteBuffer indexBuf, MaterialLoader loader, MaterialFactories materials, boolean maximize) {
		Map<String, Map<String, GLElement>> sortedElements = new HashMap<String, Map<String, GLElement>>();
		Map<String, GLElement> subElement = null;
		for (String name: build.groups.keySet()) {
			if (build.groups.get(name).isEmpty()) {
				System.err.println("loadElements: skipping empty group " + name);
				continue;
			}
			System.err.println("loadElements: loading group " + name);
			String[] nameParts = name.split("_");
			subElement = sortedElements.get(nameParts[0]);
			if (subElement == null) {
				subElement = new HashMap<String, GLElement>();
				sortedElements.put(nameParts[0], subElement);
			}
			GLMaterial material = getMaterial(build, name, materials);
			subElement.put(name, loadSingleElement(build, name, vertBuf, indexBuf, loader, material, maximize));
		}
		Map<String, GLElementGroup> groups = new HashMap<String, GLElementGroup>(sortedElements.size());
		for (String name: sortedElements.keySet()) {
			GLElement[] elems = new GLElement[sortedElements.get(name).size()];
			int idx = 0;
			for (GLElement e: sortedElements.get(name).values()) {
				elems[idx++] = e;
			}
			BoundingBox bbox = new BoundingBox(elems[0].bbox);
			for (GLElement e: elems) {
				bbox.addBox(e.bbox);
			}
			groups.put(name, new GLElementGroup(elems, bbox));
		}
		return groups;
	}
	
	public static GLMaterial getMaterial(Build build, String name, MaterialFactories materials) {
		return materials.getMaterial(getMaterialIndex(build, name));
	}
	
	public static int getMaterialIndex(Build build, String name) {
		Material m = build.groups.get(name).get(0).material;
		if (m.mapKdFilename != null) {
			return ObjSaver.MATERIAL_DIFFUSE_TEXTURED_ID;
		} else if (m.bumpFilename != null) {
			return ObjSaver.MATERIAL_NORMAL_TEXTURED_ID;
		} else {
			return ObjSaver.MATERIAL_UNIFORM_ID;
		}
	}
	
	public static boolean needsTangentCalculation(Build build, String name) {
		return getMaterialIndex(build, name) == ObjSaver.MATERIAL_NORMAL_TEXTURED_ID;
	}
	
	public static boolean needsTextures(Material m) {
		return m.mapDFilename != null  || m.mapKaFilename != null || m.mapKdFilename != null
		    || m.mapKsFilename != null || m.mapNsFilename != null || m.bumpFilename != null;
	}
	public static GLElement loadSingleElement(Build build, String name, ByteBuffer vertBuf, ByteBuffer indexBuf, MaterialLoader loader, GLMaterial material, boolean maximize) {
		System.err.println("objhelper: loading element \"" + name + "\"");
		List<Face> faces = build.groups.get(name);
		if (faces.isEmpty()) {
			System.err.println("objhelper: element is empty!");
			return null;
		}
		Material groupMaterial = faces.get(0).material;
		System.err.println("objhelper: material is " + groupMaterial.name);
		if (maximize || needsTangentCalculation(build, name)) {
			calculateTangents(build, name);
		}
		// FIXME 
		int stride = maximize ? 44 : material.getFullStride();
		boolean needsTextures = maximize || needsTextures(groupMaterial);

		int lowestIndex = faces.get(0).vertices.get(0).index; // a guess, but probably a safe one
		int highestIndex = 0;
		int baseOffset = vertBuf.position();
		System.err.println("objhelper: current offset: 0x" + Integer.toHexString(baseOffset));
		VertexGeometric tmp = faces.get(0).vertices.get(0).v;
		BoundingBox bbox = new BoundingBox(tmp.x, tmp.y, tmp.z);
		for (Face face: faces) {
			for (FaceVertex vert: face.vertices) {
				bbox.addPoint(vert.v.x, vert.v.y, vert.v.z);
				int index = vert.index - lowestIndex;
				highestIndex = highestIndex > index ? highestIndex : index;
				try {
					vertBuf.position(baseOffset + stride * index);
					addToBuffer(vert, vertBuf, needsTextures);
				} catch (BufferOverflowException e) {
					System.err.println("loadSingleElement" + String.format("overflowed buffer:\nbase offset 0x%x,\n" +
							"lowest index %d,\n" +
							"current highest index %d,\n" +
							"current index %d,\n" +
							"buffer size %d,\n" +
							"buffer position 0x%x",
							baseOffset, lowestIndex, highestIndex, index, vertBuf.capacity(), vertBuf.position()));
					throw e;
				}
			}
		}
		vertBuf.position(baseOffset + stride * (highestIndex + 1));
		int indexOffset = indexBuf.position();
		addFaceArray(faces, indexBuf, lowestIndex);
		
		Shape shape = new Shape(indexOffset, getIndexCount(build, name));
		System.err.println("objhelper: vertex count is " + getVertexCount(build, name) + "; highest vertex index is " + highestIndex);
		GLElement element = new GLElement(shape, material, material.createSave(groupMaterial), baseOffset, bbox);
		return element;
	}
	
	public static void addFaceArray(List<Face> faces, ByteBuffer indexBuffer, int indexOffset) {
		System.err.println("objhelper: adding " + faces.size() + " face indices");
		for (Face f: faces) {
			addIndices(f, indexBuffer, indexOffset);
		}
	}
	
	public static void addIndices(Face f, ByteBuffer indexBuffer, int indexOffset) {
		for (FaceVertex fv: f.vertices) {
			indexBuffer.putShort((short) (fv.index - indexOffset));
		}
	}
	
	public static void addToBuffer(FaceVertex vertex, ByteBuffer vertBuffer, boolean needsTextures) {
		addToBuffer(vertex.v, vertBuffer);
		addToBuffer(vertex.n, vertBuffer);
		if (needsTextures) {
			addToBuffer(vertex.t, vertBuffer);
		}
		if (vertex.a != null) {
			addToBuffer(vertex.a, vertBuffer);
		}
	}
	
	public static void addToBuffer(VertexGeometric coord, ByteBuffer vertBuffer) {
//		log("geom-vert", "adding " + Arrays.toString(new float[] { coord.x, coord.y, coord.z }));
		vertBuffer.putFloat(coord.x);
		vertBuffer.putFloat(coord.y);
		vertBuffer.putFloat(coord.z);
	}
	public static void addToBuffer(VertexNormal coord, ByteBuffer vertBuffer) {
		if (coord == null) {
//			log("norm-vert", "adding " + Arrays.toString(new float[] { 0f, 0f, 0f }));
			vertBuffer.putFloat(0.0f);
			vertBuffer.putFloat(0.0f);
			vertBuffer.putFloat(0.0f);
		} else {
//			log("norm-vert", "adding " + Arrays.toString(new float[] { coord.x, coord.y, coord.z }));
			vertBuffer.putFloat(coord.x);
			vertBuffer.putFloat(coord.y);
			vertBuffer.putFloat(coord.z);
		}
	}
	
	public static void addToBuffer(VertexTexture coord, ByteBuffer vertBuffer) {
		if (coord == null) {
//			log("text-vert", "adding " + Arrays.toString(new float[] { 0.5f, 0.5f }));
			vertBuffer.putFloat(0.5f);
			vertBuffer.putFloat(0.5f);
		} else {
//			log("text-vert", "adding " + Arrays.toString(new float[] { coord.u, coord.v }));
			vertBuffer.putFloat(coord.u);
			vertBuffer.putFloat(coord.v);
		}
	}
	
	public static void addToBuffer(VertexTangent coord, ByteBuffer vertBuffer) {
		if (coord == null) {
//			log("tangent-vert", "adding " + Arrays.toString(new float[] { 0f, 0f, 0f }));
			vertBuffer.putFloat(0.0f);
			vertBuffer.putFloat(0.0f);
			vertBuffer.putFloat(0.0f);
		} else {
//			log("tangent-vert", "adding " + Arrays.toString(new float[] { coord.x, coord.y, coord.z }));
			vertBuffer.putFloat(coord.x);
			vertBuffer.putFloat(coord.y);
			vertBuffer.putFloat(coord.z);
		}
	}
	
	public static int getVertexCount(Build build, String group) {
		return build.groups.get(group).size() * 3;
	}
	public static int getIndexCount(Build build) {
		return build.faces.size() * 3; // probably a safe assumption
	}
	public static int getIndexCount(Build build, String group) {
		return build.groups.get(group).size() * 3; // probably a safe assumption
	}
	public static int getVertexSize(Build build, MaterialFactories materials) {
		int size = 0;
		int count = 0;
		int offset = 0;
		for (String name: build.groups.keySet()) {
			int vsize = getVertexSize(build, name, materials);
			size += vsize;
			offset += vsize;
			count += getVertexCount(build, name);
			offset += getIndexSize(build, name);
			System.err.println("getvertexsize: estimated offset: 0x" + Integer.toHexString(offset));
		}
		System.err.println("getvertexsize: total vertex size: " + size + ", for " + count + "estimated vertices");
		System.err.println("getvertexsize: (actual total vertices: " + build.faceVerticeList.size() + ")");
		return size;
	}
	public static int getVertexSize(Build build, String group, MaterialFactories materials) {
		if (build.groups.get(group).isEmpty()) {
			return 0;
		}
		int strideSize = getMaterial(build, group, materials).getFullStride();
		System.err.println("getvertexsize: Group " + group + ", material id " + getMaterialIndex(build, group) + ", " +
		getVertexCount(build, group) + " estimated vertices, for " + strideSize * getVertexCount(build, group) + " bytes");
		return strideSize * getVertexCount(build, group);
	}
	public static int getIndexSize(Build build) {
		System.err.println("getindexsize: total indices: " + getIndexCount(build) + ", should occupy " + 2 * getIndexCount(build) + "bytes");
		return 2 * getIndexCount(build);
	}
	public static int getIndexSize(Build build, String group) {
		return 2 * getIndexCount(build, group);
	}
	public static int getSize(Build build, MaterialFactories materials) {
		return getVertexSize(build, materials) + getIndexSize(build);
	}
	
	public static void calculateTangents(Build build, String group) {
		Map<FaceVertex, List<Face>> vertexFaceMap = new HashMap<FaceVertex, List<Face>>();
		List<Face> faces = build.groups.get(group);
		// TODO have the builder make lists of faces belonging to each vertex because this is silly
		for (Face f: faces) {
			f.calculateTriangleNormal();
			f.calculateTriangleTangent();
			for (FaceVertex fv: f.vertices) {
				List<Face> vf = vertexFaceMap.get(fv);
				if (vf == null) {
					vf = new ArrayList<Face>();
					vertexFaceMap.put(fv, vf);
				}
				vf.add(f);
			}
		}
		for (FaceVertex fv: vertexFaceMap.keySet()) {
			List<Face> vf = vertexFaceMap.get(fv);
			VertexTangent vt = new VertexTangent(0, 0, 0);
			for (Face f: vf) {
				vt.add(f.faceTangent);
			}
			vt.normalize();
			fv.a = vt;
		}
	}

}
