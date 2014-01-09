package com.github.wartman4404.objpackager;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.github.wartman4404.objpackager.gl.ObjSaver;
import com.owens.oobjloader.parser.MaterialLoader;
import com.owens.oobjloader.parser.Parse;


public class Main {
	public static void main(String[] args) {
		boolean maximize = false;
		if (args.length > 0 && args[0].equals("-m")) {
			System.out.println("to the max!");
			String[] a2 = new String[args.length-1];
			System.arraycopy(args, 1, a2, 0, a2.length);
			args = a2;
			maximize = true;
		}
		if (args.length == 0) {
			System.out.println("Please specify a .obj file to convert.");
			return;
		}
		String filename = args[0];
		File file = new File(filename);
		if (!file.exists()) {
			System.out.println(filename + " doesn't exist!");
			return;
		}
		String outfilename = file.getName().substring(0, file.getName().lastIndexOf(".obj")) + ".jbo";
		MaterialLoader loader = new Parse.FilesystemLoader(filename);
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			GLRenderer gl = new GLRenderer(in, loader, maximize);
			in.close();
			OutputStream materialOut = new BufferedOutputStream(new FileOutputStream(outfilename + "m"));
			OutputStream elementOut = new BufferedOutputStream(new FileOutputStream(outfilename + "v"));
			gl.saveFile(materialOut, elementOut);
			materialOut.close();
			elementOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done converting.");
		
		try {
			System.out.println("Now trying to load converted file...");
			InputStream materialIn = new BufferedInputStream(new FileInputStream(outfilename + "m"));
			InputStream elementIn = new BufferedInputStream(new FileInputStream(outfilename + "v"));
			GLRenderer gl = new GLRenderer(materialIn, elementIn, loader, new ObjSaver.DefaultMaterialFactories());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Done reloading!");
		
		
	}
}
