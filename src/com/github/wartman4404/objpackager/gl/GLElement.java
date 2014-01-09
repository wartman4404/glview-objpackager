package com.github.wartman4404.objpackager.gl;

public class GLElement {
	private final Shape shape;
	private final GLMaterialSave materialProps;
	private int baseOffset;
	public GLElement(Shape shape, GLMaterial factory, GLMaterialSave materialProps, int baseOffset, BoundingBox bbox) {
		this.shape = shape;
		this.materialProps = materialProps;
		this.baseOffset = baseOffset;
		this.bbox = bbox;
	}
	public GLMaterialSave getMaterial() {
		return materialProps;
	}
	public int getVertexOffset() {
		return baseOffset;
	}
	public int getIndexOffset() {
		return shape.getIndexOffset();
	}
	public int getIndexCount() {
		return shape.getIndexCount();
	}
	public BoundingBox bbox;
}