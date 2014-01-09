package com.github.wartman4404.objpackager.gl;

public class Shape {
    private final int indexOffset;
    private final int indexCount;

    public Shape(int indexOffset, int indexCount) {
    	this.indexCount = indexCount;
    	this.indexOffset = indexOffset;
    }

    public int getIndexOffset() {
    	return indexOffset;
    }
    public int getIndexCount() {
    	return indexCount;
    }
}