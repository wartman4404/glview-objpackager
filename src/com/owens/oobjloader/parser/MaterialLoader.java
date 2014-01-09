package com.owens.oobjloader.parser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public interface MaterialLoader {
	public InputStream getMaterialStream(String name) throws FileNotFoundException, IOException;
}
