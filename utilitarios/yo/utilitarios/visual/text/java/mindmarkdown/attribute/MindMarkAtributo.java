package utilitarios.visual.text.java.mindmarkdown.attribute;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import utilitarios.ferramenta.regex.RegexBuilder;
import utilitarios.visual.text.java.mindmarkdown.MindMarkDocumento;
import utilitarios.visual.text.java.mindmarkdown.MindMarkEditor;
@SuppressWarnings({"serial","unchecked"})
public class MindMarkAtributo extends SimpleAttributeSet implements RegexBuilder{
//ESCAPE
	protected final static String ESCAPE_TAG=Pattern.quote("\\");
//VAR STATICS
	//DEFAULT
	public final static String DEFAULT_FORMAT="default-format";
		public static void setDefaultFormat(MindMarkAtributo atributo){
			atributo.addAttribute(DEFAULT_FORMAT,true);
		}
	//HIDDEN
	public final static String HIDDEN_FORMAT="hidden-format";
		public static void setHiddenFormat(MindMarkAtributo atributo,boolean show){
			atributo.addAttribute(HIDDEN_FORMAT,show);
		}
	//SPECIAL
	public final static String SPECIAL_FORMAT="special-format";
		public static void setSpecialFormat(MindMarkAtributo atributo){
			atributo.addAttribute(SPECIAL_FORMAT,true);
		}
	public final static MindMarkAtributo SPECIAL=new MindMarkAtributo(){{
		MindMarkAtributo.setSpecialFormat(this);
		StyleConstants.setFontFamily(this,MindMarkEditor.DEFAULT_FONT.getFamily());
		StyleConstants.setForeground(this,MindMarkEditor.SPECIAL_CHARACTERS_COLOR);
	}};
	public final static MindMarkAtributo INVISIBLE=new MindMarkAtributo(){{
		StyleConstants.setFontSize(this,0);
	}};
	//SECTION
	public final static String SECTION_FORMAT="section-format";
		public static void setSectionFormat(MindMarkAtributo atributo,List<MindMarkDocumento.FormatSection>section){
			atributo.addAttribute(SECTION_FORMAT,section);
		}
//MATCH
	protected class Match{
	//INDEX
		private int index=-1;
			public int getIndex(){return index;}
			public void setIndex(int index){this.index=index;}
	//LENGTH
		private int length=-1;
			public int getLength(){return length;}
			public void setLength(int length){this.length=length;}
	//MAIN
		public Match(){}
		public Match(int index,int length){
			setIndex(index);
			setLength(length);
		}
	//FUNCS
		public boolean isEmpty(){return (index==-1&&length==-1);}
		public void reset(){
			index=-1;
			length=-1;
		}
	}
//ATRIBUTE_LIST
	public static class AtributeList<T> extends ArrayList<T>{
	//INDEX
		private int index=0;
	//MAIN
		public void setKey(int index){this.index=index;}
	@Override
		public int hashCode(){
			final int prime=31;
			int result=super.hashCode();
			result=prime*result+index;
			return result;
		}
	@Override
		public boolean equals(Object obj){
			if(!super.equals(obj))return false;
			final AtributeList<T>list=(AtributeList<T>)obj;
			if(index!=list.index)return false;
			return true;
		}
	}
//MAIN
	public MindMarkAtributo(){}
//FUNCS: ESCAPE
	//DEFINITION
	protected String escapeDefinition(String name){
		return//	(?:(?<escape>(?<!\\)\\(?:\\\\)*?)|(?<nonEscape>(?<!\\)(?:\\\\)*?))
				pseudoGroup(oneOrOther(
						namedGroup("escape"+name,
								notPrecededBy(ESCAPE_TAG)+
								ESCAPE_TAG+
								pseudoGroup(ESCAPE_TAG+ESCAPE_TAG)+zeroOrMore()+butInTheSmallestAmount()
						),
						namedGroup("nonEscape"+name,
								notPrecededBy(ESCAPE_TAG)+
								pseudoGroup(ESCAPE_TAG+ESCAPE_TAG)+zeroOrMore()+butInTheSmallestAmount()
						)
				));
	}
		public String escapeDefinition(){return escapeDefinition("");}
//FUNCS: STYLE
	//STYLE_ESCAPE
	protected static void styleEscape(MindMarkDocumento doc,int indexEscape,int lengthEscape,int lengthTag){
		for(int i=0;i<lengthEscape;i+=2)doc.setCharacterAttributes(indexEscape+i,1,MindMarkAtributo.SPECIAL,true);
		registerAtributo(doc,new MindMarkDocumento.FormatSection(indexEscape,lengthEscape,indexEscape,lengthEscape));
	}
	//STYLE_NON_ESCAPE
	protected static void styleNonEscape(MindMarkDocumento doc,int indexEscape,int lengthEscape,int lengthTag){
		if(lengthEscape==0||lengthTag==0)return;
		styleEscape(doc,indexEscape,lengthEscape,lengthTag);	//EXATAMENTE IGUAL A ESCAPE
		registerAtributo(doc,new MindMarkDocumento.FormatSection(indexEscape,lengthEscape,indexEscape,lengthEscape));
	}
	//STYLE
	protected static void styleText(MindMarkDocumento doc,int indexTagStart,int lengthTagStart,MindMarkAtributo atributo,int indexTagEnd,int lengthTagEnd){
		style(doc,indexTagStart,lengthTagStart,atributo,indexTagEnd,lengthTagEnd,true);
	}
	protected static void styleLine(MindMarkDocumento doc,int indexTagStart,int lengthTagStart,MindMarkAtributo atributo,int indexTagEnd,int lengthTagEnd){
		style(doc,indexTagStart,lengthTagStart,atributo,indexTagEnd,lengthTagEnd,false);
	}
	private static void style(MindMarkDocumento doc,int indexTagStart,int lengthTagStart,MindMarkAtributo atributo,int indexTagEnd,int lengthTagEnd,boolean isText){
		//TAG_START
		doc.setCharacterAttributes(indexTagStart,lengthTagStart,MindMarkAtributo.SPECIAL,true);
		//TEXT
		final int indexTexto=indexTagStart+lengthTagStart;
		final int lengthTexto=indexTagEnd-indexTexto;
		if(isText){
			doc.setCharacterAttributes(indexTexto,lengthTexto,atributo,false);
		}else doc.setParagraphAttributes(indexTexto,lengthTexto,atributo,false);
		//TAG_END
		doc.setCharacterAttributes(indexTagEnd,lengthTagEnd,MindMarkAtributo.SPECIAL,true);
		//FINISH
		registerAtributo(doc,new MindMarkDocumento.FormatSection(indexTagStart,lengthTagStart,indexTagEnd,lengthTagEnd));
	}
//FUNCS
	protected static void registerAtributo(MindMarkDocumento doc,MindMarkDocumento.FormatSection section){
		int index=section.getStartTagIndex();
		final Element rootElement=doc.getDefaultRootElement();
		final int startElemIndex=rootElement.getElementIndex(index);
		int endElemIndex=rootElement.getElementIndex(section.getEndTagIndex());
		if(endElemIndex<startElemIndex)endElemIndex=startElemIndex;
		for(int i=startElemIndex;i<=endElemIndex;i++){
			final Element paragraph=rootElement.getElement(i);
			final AtributeList<MindMarkDocumento.FormatSection>sections=
					(AtributeList<MindMarkDocumento.FormatSection>)paragraph.getAttributes().getAttribute(MindMarkAtributo.SECTION_FORMAT);
			if(sections==null){
				final AtributeList<MindMarkDocumento.FormatSection>newSections=new AtributeList<>();
				newSections.setKey(i);
				newSections.add(section);
				final MindMarkAtributo formatAtributo=new MindMarkAtributo();
				MindMarkAtributo.setSectionFormat(formatAtributo,newSections);
				doc.setParagraphAttributes(paragraph.getStartOffset(),0,formatAtributo,false);
			}else sections.add(section);
		}
	}
	protected static boolean isStyledSpecial(MindMarkDocumento doc,int index){
		final Boolean isStyled=(Boolean)doc.getCharacterElement(index).getAttributes().getAttribute(SPECIAL_FORMAT);
		if(isStyled!=null&&isStyled==true)return true;
		return false;
	}
}