package com.github.wartman4404.objpackager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

import com.github.wartman4404.objpackager.gl.GLElementGroup;
import com.github.wartman4404.objpackager.gl.GLMaterial;
import com.github.wartman4404.objpackager.gl.ObjSaver;
import com.github.wartman4404.objpackager.gl.ObjSaver.MaterialFactories;
import com.github.wartman4404.objpackager.gl.ObjSaver.VBOData;
import com.github.wartman4404.objpackager.material.DiffuseTexturedMaterialFactory;
import com.github.wartman4404.objpackager.material.UniformColorMaterialFactory;
import com.owens.oobjloader.builder.Build;
import com.owens.oobjloader.parser.MaterialLoader;
import com.owens.oobjloader.parser.Parse;

public class GLRenderer {
	
    protected Map<String, GLElementGroup> mGLElements;
    private GLMaterial[] mGLMaterials;
    private MaterialFactories factories;
    
//    private FaceVertex maxVert;
    
    private ByteBuffer vertBuf;
    private ByteBuffer idxBuf;
    
    private MaterialLoader loader;
    
    public GLRenderer(InputStream in, MaterialLoader loader, boolean maximize) throws IOException {
    	this.loader = loader;
    	loadObj(in, maximize);
    }
    
    public GLRenderer(InputStream materialIn, InputStream elementIn, MaterialLoader loader, MaterialFactories factories) throws IOException {
    	this.loader = loader;
    	this.factories = factories;
    	loadFile(materialIn, elementIn);
    }
    
    public void loadFile(InputStream materialIn, InputStream elementIn) throws IOException {
    	VBOData data = ObjSaver.loadFile(materialIn, elementIn, factories);
    	System.err.println("loaded file");
    	mGLElements = data.elements;
    	mGLMaterials = data.materials;
    	vertBuf = data.vertBuf;
    	idxBuf = data.idxBuf;
    }
    
    public void loadObj(InputStream in, boolean maximize) throws IOException {
    	Build objBuilder = new com.owens.oobjloader.builder.Build();
    	Parse parse = new com.owens.oobjloader.parser.Parse(objBuilder, in, loader);
    	MaterialFactories materials = new ObjSaver.DefaultMaterialFactories();
    	ByteBuffer vertBuf = ByteBuffer.allocateDirect(ObjHelper.getVertexSize(objBuilder, materials));
    	ByteBuffer idxBuf = ByteBuffer.allocateDirect(ObjHelper.getIndexSize(objBuilder));
    	vertBuf.order(ByteOrder.nativeOrder());
    	idxBuf.order(ByteOrder.nativeOrder());
    	
    	Map<String, GLElementGroup> elements = ObjHelper.loadElements(objBuilder, vertBuf, idxBuf, loader, materials, maximize);
    	
    	vertBuf.flip();
    	idxBuf.flip();
    	this.vertBuf = vertBuf;
    	this.idxBuf = idxBuf;
    	
    	mGLElements = elements;
    }
    
    public void saveFile(OutputStream materialOut, OutputStream elementOut) throws IOException {
    	//TODO: am I closing any of these files correctly?
    	ObjSaver.saveObject(mGLElements, vertBuf, idxBuf, new DataOutputStream(materialOut), new DataOutputStream(elementOut));
    }

}
