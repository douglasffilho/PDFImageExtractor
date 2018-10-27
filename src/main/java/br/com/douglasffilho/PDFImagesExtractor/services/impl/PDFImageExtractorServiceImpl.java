package br.com.douglasffilho.PDFImagesExtractor.services.impl;

import br.com.douglasffilho.PDFImagesExtractor.services.PDFImageExtractorService;
import com.itextpdf.text.pdf.PRIndirectReference;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PDFImageExtractorServiceImpl implements PDFImageExtractorService {
	
	@Override
	public List<byte[]> getImagesFromPDF(final File pdfFile) throws Exception {
		final InputStream is = new FileInputStream(pdfFile);
		
		final List<byte[]> images = new ArrayList<>();
		
		final PdfReader pdf = new PdfReader(is);
		
		final int numberOfPages = pdf.getNumberOfPages();
		
		for (int i = 1; i <= numberOfPages; i++) {
			final PdfDictionary page = pdf.getPageN(i);
			final PdfDictionary resource = (PdfDictionary) PdfReader.getPdfObject(page.get(PdfName.RESOURCES));
			final PdfDictionary xObject = (PdfDictionary) PdfReader.getPdfObject(resource.get(PdfName.XOBJECT));
			
			if (xObject != null) {
				for (final Iterator it = xObject.getKeys().iterator(); it.hasNext(); ) {
					final PdfObject obj = xObject.get((PdfName) it.next());
					
					if (obj.isIndirect()) {
						final PdfDictionary tg = (PdfDictionary) PdfReader.getPdfObject(obj);
						final PdfName type = (PdfName) PdfReader.getPdfObject(tg.get(PdfName.SUBTYPE));
						
						if (PdfName.IMAGE.equals(type)) {
							//pego os bytes do meu objeto
							final int XrefIndex = ((PRIndirectReference) obj).getNumber();
							final PdfObject pdfObj = pdf.getPdfObject(XrefIndex);
							final PdfStream pdfStrem = (PdfStream) pdfObj;
							images.add(PdfReader.getStreamBytesRaw((PRStream) pdfStrem));
						}
					}
				}
			}
		}
		
		pdf.close();
		is.close();
		
		return images;
	}
}
