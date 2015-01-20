package cn.chinattclub.searcher.service;

import java.util.List;

import cn.chinattclub.searcher.exception.ServiceException;

/**
 * TODO Put here a description of what this class does.
 * 
 * @author zhangying. Created 2015-1-20.
 */
public abstract interface SearcherEngineService {

	public abstract String search(String paramString1, String paramString2,
			List<String> paramList, String paramString3, int paramInt1,
			int paramInt2, String paramString4, boolean paramBoolean)
			throws ServiceException;

}
