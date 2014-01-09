package com.github.wartman4404.objpackager.material;

import java.io.DataInputStream;
import java.io.IOException;

import com.github.wartman4404.objpackager.gl.GLMaterialSave;
import com.github.wartman4404.objpackager.gl.ObjSaver;
import com.owens.oobjloader.builder.Material;

public class DiffuseTexturedMaterialFactory extends AbstractTexturedMaterialFactory {

	public class DiffuseTexturedMaterial extends AbstractTexturedMaterial {
		public DiffuseTexturedMaterial(DiffuseTexturedMaterialSave save) {
			super(save);
		}
	}

	@Override
	public GLMaterialSave createSave(Material props) {
		return new DiffuseTexturedMaterialSave(props);
	}
	public static class DiffuseTexturedMaterialSave extends AbstractTextureSave {
		public DiffuseTexturedMaterialSave(DataInputStream in) throws IOException {
			super(in);
		}
		public DiffuseTexturedMaterialSave(Material props) {
			super(props, props.mapKdFilename);
		}

		@Override
		public int getId() {
			return ObjSaver.MATERIAL_DIFFUSE_TEXTURED_ID;
		}
	}
}