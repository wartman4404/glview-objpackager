package com.github.wartman4404.objpackager.gl;


public interface GLMaterial {
	public GLMaterialSave createSave(com.owens.oobjloader.builder.Material props);
	public int getFullStride();

	public interface GLMaterialInstance { }
}