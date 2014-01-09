package com.github.wartman4404.objpackager.material;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import com.github.wartman4404.objpackager.gl.ObjSaver;
import com.owens.oobjloader.builder.Material;

public abstract class AbstractTexturedMaterialFactory extends PhongShadedMaterial {
	
	public class AbstractTexturedMaterial extends PhongShadedInstance {

		public AbstractTexturedMaterial(AbstractTextureSave save) {
			super(save);
		}
	}
	public static final int TEXTURE_STRIDE = 2 * 4;
	public static final int TEXTURE_OFFSET = NORMAL_OFFSET + NORMAL_STRIDE;
	public static final int FULL_STRIDE = COORD_STRIDE + NORMAL_STRIDE + TEXTURE_STRIDE;
	public static int getTextureStride() { return TEXTURE_STRIDE; }
	public static int getTextureOffset() { return TEXTURE_OFFSET; }

	public int getFullStride() {
		return COORD_STRIDE + NORMAL_STRIDE + TEXTURE_STRIDE;
	}
}
abstract class AbstractTextureSave extends PhongSave {
	protected String textureFilename;

	public AbstractTextureSave(Material props, String filename) {
		super(props);
		textureFilename = new File(filename).getName();
	}
	public AbstractTextureSave(DataInputStream in) throws IOException {
		super(in);
		textureFilename = ObjSaver.readString(in);
	}

	@Override
	public void save(DataOutputStream out) throws IOException {
		super.save(out);
		ObjSaver.saveString(out, textureFilename);
	}

	@Override
	public int getId() {
		return -1;
	}
	
}