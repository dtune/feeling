package jp.co.nttdata.rate.model.rateKey;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.configuration.ConfigurationRuntimeException;
import org.apache.commons.configuration.beanutils.BeanDeclaration;
import org.apache.commons.configuration.beanutils.DefaultBeanFactory;
import org.apache.commons.lang.ClassUtils;

public class RateKeyBeanFactory extends DefaultBeanFactory {
    
	
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
        return RateKey.class;
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
		        initProperty(bean, propName, e.getValue());
		    }
		}

		Map nestedBeans = data.getNestedBeanDeclarations();
		if (nestedBeans != null)
		{
		    for (Iterator it = nestedBeans.entrySet().iterator(); it.hasNext();)
		    {
		        Map.Entry e = (Map.Entry) it.next();
		        String propName = (String) e.getKey();
		        
		        //itemsÇÃèÍçáÅAitemÇÇPÇ∏Ç¬èâä˙âªÇ∑ÇÈ
		        if (propName.equals(RateKeyDeclaration.ITEM_NODE)) {
		        	for (Object obj : (List)e.getValue()) {
		        		BeanDeclaration decl = (BeanDeclaration)obj;
				        Class clz = ClassUtils.getClass(decl.getBeanClassName());
				        Item item = (Item) createBean(clz, decl, null);
				        ((List)bean).add(item);
		        	}
		        } else {
			        BeanDeclaration decl = (BeanDeclaration) e.getValue();
			        Class clz = ClassUtils.getClass(decl.getBeanClassName());
			        initProperty(bean, propName, createBean(clz, decl, null));
		        }

		    }
		}

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
	private  void initProperty(Object bean, String propName, Object value)
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
