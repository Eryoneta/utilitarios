package utilitarios.visual.text.java.mindmarkdown;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.ViewFactory;
import utilitarios.visual.text.java.SimpleEditor;
import utilitarios.visual.text.java.mindmarkdown.view.MMViewFactory;
@SuppressWarnings("serial")
public class MindMarkEditor extends SimpleEditor{
//INTERFACE
	private final MMViewFactory viewFactory=new MMViewFactory(this);
	@Override
		public ViewFactory getViewFactory(){return viewFactory;}
//TEXTO
	private MindMarkTexto editorTexto;
		public MindMarkTexto getTextEditor(){return editorTexto;}
//DOCUMENTO
	private MindMarkDocumento documento=new MindMarkDocumento();
		public MindMarkDocumento getMindMarkDocumento(){return documento;}
//IMAGENS
	private HashMap<String,BufferedImage>imagens=new HashMap<String,BufferedImage>();	//TODO: CUIDADO, ITENS NÃO SÃO REMOVIDOS!
		public HashMap<String,BufferedImage>getLoadedImages(){return imagens;}
//MAIN
	public MindMarkEditor(MindMarkTexto editorTexto){
		this.editorTexto=editorTexto;
	}
	public void build(){
		editorTexto.setDocument(documento);
		documento.addDocumentListener(new DocumentListener(){
		@Override public void removeUpdate(DocumentEvent d){update(d);}
		@Override public void insertUpdate(DocumentEvent d){update(d);}
		@Override public void changedUpdate(DocumentEvent d){}
			private void update(DocumentEvent d){
				SwingUtilities.invokeLater(new Runnable(){
					@Override public void run(){
						updateInterface();
					}
				});
			}
			private synchronized void updateInterface(){
				final int viewMode=editorTexto.getFormatViewMode();
				final int indexStart=Math.min(editorTexto.getSelectionStart(),editorTexto.getSelectionEnd());
				final int indexEnd=Math.max(editorTexto.getSelectionStart(),editorTexto.getSelectionEnd());
				documento.updateAllMarkdown(viewMode,indexStart,indexEnd);
			}
		});
		editorTexto.addCaretListener(new CaretListener(){
		@Override public void caretUpdate(CaretEvent c){
				final int indexStart=Math.min(c.getDot(),c.getMark());
				final int indexEnd=Math.max(c.getDot(),c.getMark());
				new Thread(new Runnable(){
					@Override public void run(){
						updateInterface(indexStart,indexEnd);
					}
				}).start();
			}
			private synchronized void updateInterface(int indexStart,int indexEnd){
				final int viewMode=editorTexto.getFormatViewMode();
				documento.updateAllMarkdownVisibility(viewMode,indexStart,indexEnd);
			}
		});
	}
@Override
	public void read(Reader in,Document doc,int pos)throws IOException, BadLocationException{
//		super.read(in,doc,pos);		//CHAMA insertString() VÁRIAS VEZES(ERRADO!)
		final BufferedReader reader=new BufferedReader(in);
		final StringBuffer content=new StringBuffer();
		String line;
		while((line=reader.readLine())!=null){
			content.append(line);
			content.append("\n");
		}
		doc.insertString(pos,content.substring(0,content.length()-1),SimpleAttributeSet.EMPTY);
	}
}