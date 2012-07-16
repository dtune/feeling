package jp.co.nttdata.rate.fms.core.keyword;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jp.co.nttdata.rate.fms.core.Parentheses;
import jp.co.nttdata.rate.fms.core.Range;
import jp.co.nttdata.rate.fms.core.Sequence;
import jp.co.nttdata.rate.fms.core.Token;

/**
 * �L�[���[�h�̗L���͈͂𔻖����Čv�Z����
 * @author btchoukug
 *
 */
public abstract class Keyword implements Comparable<Keyword> {

	public enum KeywordType {SET, WHILE, IF, ELSEIF, ELSE, SUM, MULT}
	
	private static final long serialVersionUID = 1L;
	
	/** set�L�[���[�h */
	public static final String SET = "set";
	/** while�L�[���[�h */
	public static final String WHILE = "while";
	/** if,elseIf,else�L�[���[�h */
	public static final String IF = "if";
	public static final String ELSEIF = "elseIf";
	public static final String ELSE = "else";
	/** sum�L�[���[�h */
	public static final String SUM = "sum";
	/** ���ς̃L�[���[�h */
	public static final String MULT = "mult";
	
	/**�����Ώۂ̌v�Z�V�[�P���X*/
	protected Sequence seq;
	
	protected List<Set> setBlocks = new ArrayList<Set>();
	
	protected KeywordType type;
	
	/**�L�[���[�h��*/
	protected String keyword;
	
	/**�L�[���[�h�̌v�Z����*/
	protected BigDecimal result = BigDecimal.ZERO;
	
	protected Token resultToken;
		
	/**�L�[���[�h�͈̔�*/
	protected Range r;
	
	protected Range searchRange;
	
	public Keyword(Sequence seq, int pos){
		this.seq = seq;
		this.searchRange = new Range(pos, seq.size());
	}
	
	
	/**
	 * �����D�揇�Ŏw��̃L�[���[�h���v�Ztokens����}�b�`���ꂽ1�Ԗڂ͈̔͂�Ԃ�
	 * <br>�L�[���[�h����Ō�̃}�b�`���ꂽ�N���[�Y���ʂ܂ŁA�����ƃ{�f�B���܂�
	 * <br>Range r�Ō����͈͂��w��\�Ƃ���
	 * @param seq
	 * @param keyword
	 * @param r
	 * @return
	 */
	protected Range getKeywordRange(Sequence seq, String keyword, Range search) {
		
		int searchStart = 0;
		int searchEnd = 0;
		
		//�����͈͂�ҏW
		if (search != null) {
			searchStart = search.start;
			searchEnd = search.end;
		} else {
			//�w�肵�Ă��Ȃ��ꍇ�A�I���ʒu��Vector�̒����Ƃ���
			searchEnd = seq.size();
		}
		
		//�L�[���[�h�̊J�n�ƏI���ʒu
		int start = 0;
		int end = 0;
		
		int i = searchStart;
		while (i < searchEnd) {
			//�J�����g�L�[���[�h�̊J�n�ʒu
			start = i;
			Token t = seq.get(i);			
			//�L�[���[�h�̏ꍇ
			if (t.isKeyword()) {
				String curKeyword = (String)t.token;
				//�L�[���[�h���A�͈͂��Z�o
				if (curKeyword.equals(Keyword.SET) || curKeyword.equals(Keyword.ELSE)) {
					end = Parentheses.posMatchCloseParenthese(seq, start + 1);;
				} else {
					//if,elseIf,while�̏ꍇ�A�͈͂�if(condition){body}�̂悤�Ȍ`
					int condEnd = Parentheses.posMatchCloseParenthese(seq, start + 1);;
					end = Parentheses.posMatchCloseParenthese(seq, condEnd + 1);;
				}
				
				//�w��̃L�[���[�h�̏ꍇ�A�͈͂�Ԃ�
				if (keyword.equals(curKeyword) && end != -1) {
					return new Range(start, end);
				} else {
					//�Ȃ���΁A��L�L�[���[�h�͈̔͂��X�e�b�v�Ƃ���
					i = end;					
				}				
				
			}
			
			//����token��
			i++;
		}
		
		return null;
	}
	
	/**���O�e�����E�{�f�B�ȂǕς��Ȃ������𒊏o���邱��*/
	public abstract void compile();
	
	public abstract Token calculate() throws Exception;
	
	/**�L�[���[�h�̗D�揇��Ԃ��i�Ⴂ�̂͂��͗D��Ƃ���j*/
	protected int priority() {
		switch (this.type) {
		case SET:
			return 0;
		case IF:
			return 1;
		case WHILE:
			return 2;
		case SUM:
		case MULT:
			return 3;
		default:
			return 4;
		}
		
	}
	
	/**
	 * �L�[���[�h�̗D�揇�Ń\�[�g����
	 */
	@Override
	public int compareTo(Keyword k) {
		return this.priority() - k.priority();
	}
	
	public String getName() {
		return this.keyword;
	}
	
	public KeywordType getType() {
		return this.type;
	}
	
	public Range getRange() {
		return this.r;
	}

	public Token getResultToken() {
		return this.resultToken;
	}

	public List<Set> getSetBlocks() {
		return setBlocks;
	}
	
}
