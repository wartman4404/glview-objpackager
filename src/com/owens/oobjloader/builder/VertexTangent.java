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

public class VertexTangent {
    public float x = 0;
    public float y = 0;
    public float z = 0;

    public void add(float x, float y, float z) {
	this.x += x;
	this.y += y;
	this.z += z;
    }
    
    public void add(VertexTangent other) {
    	add(other.x, other.y, other.z);
    }
    
    public void normalize() {
    	float xy = (float) Math.sqrt(x * x + y * y);
    	float length = (float) Math.sqrt(xy * xy + z * z);
    	x /= length;
    	y /= length;
    	z /= length;
    }

    public VertexTangent(float x, float y, float z) {
	this.x = x;
	this.y = y;
	this.z = z;
    }

    public String toString() {
	if(null == this)
	    return "null";
	else
	    return x+","+y+","+z;
    }
}