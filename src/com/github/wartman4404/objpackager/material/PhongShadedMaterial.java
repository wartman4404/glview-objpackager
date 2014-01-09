package com.github.wartman4404.objpackager.material;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.github.wartman4404.objpackager.gl.GLMaterial;
import com.github.wartman4404.objpackager.gl.GLMaterialSave;
import com.github.wartman4404.objpackager.gl.ObjSaver;
import com.owens.oobjloader.builder.Material;

public abstract class PhongShadedMaterial implements GLMaterial {
	protected int mPositionHandle = -1;
	protected int mNormalHandle = -1;
	protected int mProgram = -1;
	protected int mSpecularHardnessHandle = -1;
	protected int mSpecularPowerHandle = -1;
	protected int mDiffusePowerHandle = -1;
	
	public static final int COORD_STRIDE = 3 * 4;
	public static final int NORMAL_STRIDE = 3 * 4;
	
	public static final int COORD_OFFSET = 0;
	public static final int NORMAL_OFFSET = COORD_STRIDE;
	
	public static int getCoordStride() { return COORD_STRIDE; }
	public static int getNormalStride() { return NORMAL_STRIDE; }
	
	public static int getCoordOffset() { return COORD_OFFSET; }
	public static int getNormalOffset() { return NORMAL_OFFSET; }
	
	public abstract class PhongShadedInstance implements GLMaterialInstance {
		protected float mSpecularHardness;
		protected float mSpecularPower;
		protected float mDiffusePower;
		public PhongShadedInstance(PhongSave save) {
			mSpecularHardness = save.mSpecularHardness;
			// OBJ files specify an entire color, but that's more than is really called for
			mSpecularPower = save.mSpecularPower;
			mDiffusePower = save.mDiffusePower;
		}
	
		public int getProgram() {
			return mProgram;
		}
	}
}

abstract class PhongSave extends GLMaterialSave {
	protected float mSpecularHardness;
	protected float mSpecularPower;
	protected float mDiffusePower;
	protected String mName;

	public PhongSave(DataInputStream in) throws IOException {
		this.mName = ObjSaver.readString(in);
		this.mSpecularHardness = in.readFloat();
		this.mSpecularPower = in.readFloat();
		this.mDiffusePower = in.readFloat();
	}
	public PhongSave(Material props) {
		this.mSpecularHardness = (float)props.nsExponent;
		mSpecularPower = (float)props.ks.rx;
		mDiffusePower = (float)props.kd.rx;
		mName = props.name;
	}
	@Override
	public void save(DataOutputStream out) throws IOException {
		out.writeByte(this.getId());
		ObjSaver.saveString(out, mName);
		out.writeFloat(mSpecularHardness);
		out.writeFloat(mSpecularPower);
		out.writeFloat(mDiffusePower);
	}

	public String getName() {
		return mName;
	}
}
