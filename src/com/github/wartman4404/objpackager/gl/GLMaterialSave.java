package com.github.wartman4404.objpackager.gl;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class GLMaterialSave {
	public abstract int getId();
	public abstract void save(DataOutputStream out) throws IOException;
	public abstract String getName();
}