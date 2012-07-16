package jp.co.nttdata.rate.model.formula;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jp.co.nttdata.rate.log.LogFactory;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.configuration.ConfigurationRuntimeException;
import org.apache.commons.configuration.beanutils.BeanDeclaration;
import org.apache.commons.configuration.beanutils.DefaultBeanFactory;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class FormulaBeanFactory extends DefaultBeanFactory {
	
	private static Logger logger = LogFactory.getInstance(FormulaBeanFactory.class);
	
	/**�f�t�H���g�ꍇ�A�������̓R���}�Ƃ���*/
	private char listDelimiter = ',';
	
	@SuppressWarnings("unchecked")
	@Override
	public Object createBean(Class beanClass, BeanDeclaration data,
            Object parameter) throws Exception
    {
        Object result = createBeanInstance(beanClass, data);
        initBean(result, data);
        return result;
    }

    /**
     * Returns the default bean class used by this factory. This is always
     * <b>{@link RateKey}</b> for this implementation.
     *
     * @return the default bean class
     */
    @SuppressWarnings("unchecked")
    @Override
	public Class getDefaultBeanClass()
    {
        return Formula.class;
    }

    
    @SuppressWarnings("unchecked")
	public  void initBean(Object bean, BeanDeclaration data) throws Exception
	{
		Map properties = data.getBeanProperties();
		if (properties != null)
		{
		    for (Iterator it = properties.entrySet().iterator(); it.hasNext();)
		    {
		        Map.Entry e = (Map.Entry) it.next();
		        String propName = (String) e.getKey();
		        Object value = null;
		        //������`�̑���paras�ɑ΂��āA�R���}��胊�X�g�ɕϊ�����
		        if (propName.equals(FormulaDeclaration.ATTR_PARAS)) {
		        	String[] values = StringUtils.split((String)e.getValue(), this.listDelimiter);
		        	value = Arrays.asList(values);
		        } else {
		        	value = e.getValue();
		        }
		        initProperty(bean, propName, value);
		    }
		}
		
		logger.trace("����" + (Formula)bean + "�����[�h���܂����B");

		Map nestedBeans = data.getNestedBeanDeclarations();
		if (nestedBeans != null)
		{
		    for (Iterator it = nestedBeans.entrySet().iterator(); it.hasNext();)
		    {
		        Map.Entry e = (Map.Entry) it.next();
		        		        
		        //�T�u���������݂���ꍇ�A�P������������
		        List<Formula> formulaList = new ArrayList<Formula>();
	        	for (Object obj : (List)e.getValue()) {
	        		BeanDeclaration decl = (BeanDeclaration)obj;
			        Class clz = ClassUtils.getClass(decl.getBeanClassName());
			        Formula f = (Formula) createBean(clz, decl, null);
			        formulaList.add(f);
	        	}
	        	
	        	//�ҏW�������X�g��e�ɃZ�b�g����
	        	((Formula)bean).setSubFormulaList(formulaList);

		    }
		}
	
	}
    
    /**
     * �����I�Ƀ��X�g�ɕύX���邽�߂̕�������ݒ肷��
     * <br>�w�肵�Ȃ��ꍇ�A�R���}���g��
     * @param delimiter
     */
    public void setListDelimiter(char delimiter) {
    	this.listDelimiter = delimiter;
    }

	/**
	* Sets a property on the given bean using Common Beanutils.
	*
	* @param bean the bean
	* @param propName the name of the property
	* @param value the property's value
	* @throws ConfigurationRuntimeException if the property is not writeable or
	* an error occurred
	*/
	private void initProperty(Object bean, String propName, Object value)
	    throws ConfigurationRuntimeException
	{
		if (!PropertyUtils.isWriteable(bean, propName))
		{
		    throw new ConfigurationRuntimeException("Property " + propName
		            + " cannot be set!");
		}
		
			try
			{
			    BeanUtils.setProperty(bean, propName, value);
			}
			catch (IllegalAccessException iaex)
			{
			    throw new ConfigurationRuntimeException(iaex);
			}
			catch (InvocationTargetException itex)
			{
			    throw new ConfigurationRuntimeException(itex);
			}
	}
}
