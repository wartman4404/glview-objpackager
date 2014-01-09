package com.github.wartman4404.objpackager.material;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.github.wartman4404.objpackager.gl.GLMaterialSave;
import com.github.wartman4404.objpackager.gl.ObjSaver;
import com.owens.oobjloader.builder.Material;

public class NormalTexturedMaterialFactory extends AbstractTexturedMaterialFactory {

	public class NormalTexturedMaterial extends AbstractTexturedMaterial {
		public NormalTexturedMaterial(NormalTexturedMaterialSave save) {
			super(save);
		}
	}

	public static final int TANGENT_STRIDE = 3 * 4;
	public static final int TANGENT_OFFSET = TEXTURE_OFFSET + TEXTURE_STRIDE;
	public static final int FULL_STRIDE = COORD_STRIDE + NORMAL_STRIDE + TEXTURE_STRIDE + TANGENT_STRIDE;
	public static int getTangentStride() { return TANGENT_STRIDE; }
	public static int getTangentOffset() { return TANGENT_OFFSET; }

	public int getFullStride() {
		return COORD_STRIDE + NORMAL_STRIDE + TEXTURE_STRIDE + TANGENT_STRIDE;
	}

	@Override
	public GLMaterialSave createSave(Material props) {
		return new NormalTexturedMaterialSave(props);
	}

	public static class NormalTexturedMaterialSave extends AbstractTextureSave {
		protected float[] color;
		public NormalTexturedMaterialSave(DataInputStream in) throws IOException {
			super(in);
			this.color = new float[4];
			for (int i = 0; i < color.length; i++) {
				this.color[i] = in.readFloat();
			}
		}
		public NormalTexturedMaterialSave(Material props) {
			super(props, props.bumpFilename);
			this.color = new float[4];
			this.color[0] = (float) props.kd.rx;
			this.color[1] = (float) props.kd.gy;
			this.color[2] = (float) props.kd.bz;
			this.color[3] = (float) props.dFactor;
		}

		@Override
		public void save(DataOutputStream out) throws IOException {
			super.save(out);
			for (float f: this.color) {
				out.writeFloat(f);
			}
		}

		@Override
		public int getId() {
			return ObjSaver.MATERIAL_NORMAL_TEXTURED_ID;
		}
	}
}