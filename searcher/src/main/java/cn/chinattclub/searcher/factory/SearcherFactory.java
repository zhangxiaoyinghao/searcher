package cn.chinattclub.searcher.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.chinattclub.searcher.exception.ServiceException;
import cn.chinattclub.searcher.service.SearcherEngineService;


/**
 * TODO Put here a description of what this class does.
 *
 * @author zhangying.
 *         Created 2015-1-20.
 */
public class SearcherFactory {
	private static Log logger = LogFactory.getLog(SearcherFactory.class.getName());

	  public static SearcherEngineService createSearcher() throws ServiceException
	  {
	    String type = Configuration.getConfig("searcherType");
	    String searcherClass = Configuration.getConfig("searcherClass");

	    String interfaceClass = "cn.videoworks.searcher.service.SearcherEngine";

	    logger.debug("配置的搜索引擎类型：searcherType=" + type + "， searcherClass=" + searcherClass);
	    if ((type == null) || (searcherClass == null)) {
	      String logInfo = "配置的搜索引擎类型：searcherType=" + type + " 或者 searcherClass=" + searcherClass + " 不能为空 ";
	      logger.fatal(logInfo);
	      throw new ServiceException(logInfo);
	    }

	    try
	    {
	      Object obj = Class.forName(searcherClass).newInstance();

	      Class[] cs = obj.getClass().getInterfaces();

	      if ((cs == null) || (cs.length == 0)) {
	        logger.fatal("配置的搜索引擎实现类：searcherClass=" + searcherClass + " 未实现接口类：" + interfaceClass);
	        throw new ServiceException("配置的搜索引擎实现类：searcherClass=" + searcherClass + " 未实现接口类：" + interfaceClass);
	      }

	      boolean impled = false;
	      for (int i = 0; i < cs.length; i++)
	      {
	        Class c = cs[i];
	        if (c.getName().equals(interfaceClass)) {
	          impled = true;
	          break;
	        }
	      }
	      if (!impled) {
	        String logInfo = "配置的搜索引擎实现类：searcherClass=" + searcherClass + " 未实现接口类：" + interfaceClass;
	        logger.fatal(logInfo);
	        throw new ServiceException(logInfo);
	      }

	      return (SearcherEngineService)obj;
	    }
	    catch (ClassNotFoundException e) {
	      String logInfo = "配置的搜索引擎实现类：searcherClass=" + searcherClass + " 不存在 ";
	      logger.fatal(logInfo);
	      throw new ServiceException(logInfo);
	    }
	    catch (InstantiationException e) {
	      String logInfo = "实例化(" + searcherClass + ")时异常：" + e.getMessage();
	      logger.fatal(logInfo);
	      throw new ServiceException(logInfo);
	    }
	    catch (IllegalAccessException e) {
	      String logInfo = "实例化(" + searcherClass + ")时异常：" + e.getMessage();
	      logger.fatal(logInfo);
	      throw new ServiceException(logInfo);
	    }
	  }
}
