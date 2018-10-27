package br.com.douglasffilho.PDFImagesExtractor.services;


import java.io.File;
import java.util.List;

public interface PDFImageExtractorService {
	
	List<byte[]> getImagesFromPDF(File pdfFile) throws Exception;
	
}
