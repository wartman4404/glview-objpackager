package com.github.wartman4404.objpackager.material;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.github.wartman4404.objpackager.gl.GLMaterialSave;
import com.github.wartman4404.objpackager.gl.ObjSaver;
import com.owens.oobjloader.builder.Material;

public class UniformColorMaterialFactory extends PhongShadedMaterial {
	
	class UniformColorMaterial extends PhongShadedInstance {
		protected float[] color;

		public UniformColorMaterial(UniformMaterialSave save) {
			super(save);
			this.color = save.color;
		}
	}
	public static final int FULL_STRIDE = COORD_STRIDE + NORMAL_STRIDE;
	@Override
	public int getFullStride() {
		return FULL_STRIDE;
	}

	@Override
	public GLMaterialSave createSave(Material props) {
		return new UniformMaterialSave(props);
	}
	


	public static class UniformMaterialSave extends PhongSave {
		protected float[] color;
		public UniformMaterialSave(Material props) {
			super(props);
			this.color = new float[4];
			this.color[0] = (float) props.kd.rx;
			this.color[1] = (float) props.kd.gy;
			this.color[2] = (float) props.kd.bz;
			this.color[3] = (float) props.dFactor;
		}
		public UniformMaterialSave(DataInputStream in) throws IOException {
			super(in);
			this.color = new float[4];
			for (int i = 0; i < color.length; i++) {
				this.color[i] = in.readFloat();
			}
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
			return ObjSaver.MATERIAL_UNIFORM_ID;
		}
	}
}