package com.owens.oobjloader.builder;

// Written by Sean R. Owens, sean at guild dot net, released to the
// public domain. Share and enjoy. Since some people argue that it is
// impossible to release software to the public domain, you are also free
// to use this code under any version of the GPL, LPGL, Apache, or BSD
// licenses, or contact me for use of another license.

import java.util.*;
import java.text.*;
import java.io.*;
import java.io.IOException;

public class Face {

    public ArrayList<FaceVertex> vertices = new ArrayList<FaceVertex>();
    public Material material = null;
    public Material map = null;

    public Face() {
    }

    public void add(FaceVertex vertex) {
        vertices.add(vertex);
    }
    public VertexNormal faceNormal = new VertexNormal(0, 0, 0);
    
    public VertexTangent faceTangent = new VertexTangent(0, 0, 0);
    
    public void calculateEdge(float[] edge1, float[] edge2) {
        VertexGeometric v1 = vertices.get(0).v;
        VertexGeometric v2 = vertices.get(1).v;
        VertexGeometric v3 = vertices.get(2).v;
        float[] p1 = {v1.x, v1.y, v1.z};
        float[] p2 = {v2.x, v2.y, v2.z};
        float[] p3 = {v3.x, v3.y, v3.z};

        edge1[0] = p2[0] - p1[0];
        edge1[1] = p2[1] - p1[1];
        edge1[2] = p2[2] - p1[2];

        edge2[0] = p3[0] - p2[0];
        edge2[1] = p3[1] - p2[1];
        edge2[2] = p3[2] - p2[2];
    }
    
    public void calculateTextureDelta(float[] t1, float[] t2) {
    	calculateTextureCoordinates();
        VertexTexture v1 = vertices.get(0).t;
        VertexTexture v2 = vertices.get(1).t;
        VertexTexture v3 = vertices.get(2).t;
        float[] p1 = {v1.u, v1.v};
        float[] p2 = {v2.u, v2.v};
        float[] p3 = {v3.u, v3.v};
        
        t1[0] = p2[0] - p1[0];
        t1[1] = p2[1] - p1[1];
        t2[0] = p3[0] - p2[0];
        t2[1] = p3[1] - p2[1];
    }

    // @TODO: This code assumes the face is a triangle.  
    public void calculateTriangleNormal() {
        float[] edge1 = new float[3];
        float[] edge2 = new float[3];
        float[] normal = new float[3];
        calculateEdge(edge1, edge2);

        normal[0] = edge1[1] * edge2[2] - edge1[2] * edge2[1];
        normal[1] = edge1[2] * edge2[0] - edge1[0] * edge2[2];
        normal[2] = edge1[0] * edge2[1] - edge1[1] * edge2[0];

        faceNormal.x = normal[0];
        faceNormal.y = normal[1];
        faceNormal.z = normal[2];
    }

    public void calculateTriangleTangent() {
        float[] edge1 = new float[3];
        float[] edge2 = new float[3];
        float[] tangent = new float[3];
        float[] tex1 = new float[2];
        float[] tex2 = new float[2];
        calculateEdge(edge1, edge2);
        calculateTextureDelta(tex1, tex2);
        float coefficient = 1.0f / (tex1[0] * tex2[1] - tex2[0] * tex1[1]);
        
        tangent[0] = coefficient * (edge1[0] * tex2[1] + edge2[0] * -tex1[1]);
        tangent[1] = coefficient * (edge1[1] * tex2[1] + edge2[1] * -tex1[1]);
        tangent[2] = coefficient * (edge1[2] * tex2[1] + edge2[2] * -tex1[1]);
        
        faceTangent.x = tangent[0];
        faceTangent.y = tangent[1];
        faceTangent.z = tangent[2];
    }
    
    public void calculateTextureCoordinates() {
    	for (FaceVertex v: vertices) {
    		if (v.t == null) {
    			// spherical projection
    			float r = (float) Math.sqrt(v.v.x * v.v.x + v.v.y * v.v.y + v.v.z * v.v.z);
    			float theta = (float) Math.acos(v.v.z / r);
    			float phi = (float) Math.atan2(v.v.y, v.v.z);
    			v.t = new VertexTexture(theta, phi);
    		}
    	}
    }
    
    public String toString() { 
        String result = "\tvertices: "+vertices.size()+" :\n";
        for(FaceVertex f : vertices) {
            result += " \t\t( "+f.toString()+" )\n";
        }
        return result;
    }
}