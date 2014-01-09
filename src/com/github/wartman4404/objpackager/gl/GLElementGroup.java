package com.github.wartman4404.objpackager.gl;

public class GLElementGroup {
	GLElement[] elements;
	BoundingBox bbox;
	public GLElementGroup(GLElement[] elements, BoundingBox bbox) {
		this.elements = elements;
		this.bbox = bbox;
	}
	public GLElement[] getElements() {
		return elements;
	}
}