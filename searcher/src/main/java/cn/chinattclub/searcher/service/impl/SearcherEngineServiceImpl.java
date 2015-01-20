package cn.chinattclub.searcher.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.chinattclub.searcher.exception.ServiceException;
import cn.chinattclub.searcher.util.Configuration;

/**
 * TODO Put here a description of what this class does.
 *
 * @author zhangying.
 *         Created 2015-1-20.
 */
public class SearcherEngineServiceImpl {
	 private static Log logger = LogFactory.getLog(SearcherEngineServiceImpl.class.getName());

	  SolrServer hss = null;

	  static String serverURL = null;

	  public SearcherEngineServiceImpl() throws ServiceException {
	    serverURL = Configuration.getConfig("solrURL");
	    if (serverURL == null) {
	      throw new ServiceException("无法从配置文件中读取solrURL配置或者做为null");
	    }
	    if (this.hss == null)
	      try {
	        this.hss = new HttpSolrServer(serverURL);
	      } catch (Exception e) {
	        throw new ServiceException("无法创建Solr连接(solrURL=" + serverURL + ")，错误：" + e.getMessage());
	      }
	  }

	  private String createNewCondition(String databaseName)
	  {
	    String newCondition = null;
	    String[] values = databaseName.split(",");
	    for (String value : values) {
	      if (newCondition == null)
	        newCondition = value;
	      else {
	        newCondition = newCondition + " OR " + value;
	      }
	    }
	    return newCondition;
	  }

	  public String clustering(String databaseName, String category, List<String> conditions, String text, int rows)
	    throws ServiceException
	  {
	    String[] str = { "+", "-", "&", "|", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":", "\"" };
	    for (String s : str) {
	      text = text.replaceAll("\\" + s, "\\\\" + s);
	    }

	    List newConditions = escape(conditions);
	    conditions = newConditions;

	    SolrQuery query = new SolrQuery();
	    query.setRequestHandler("/clustering");
	    query.setRows(Integer.valueOf(rows < 1 ? 1000 : rows));

	    if ((databaseName != null) && (!databaseName.equals(""))) {
	      String newCondition = createNewCondition(databaseName);
	      query.addFilterQuery(new String[] { "database:" + newCondition });
	    }

	    if ((category != null) && (!category.equals(""))) {
	      query.addFilterQuery(new String[] { "catetory:" + category });
	    }

	    if (conditions != null) {
	      for (??? = conditions.iterator(); ((Iterator)???).hasNext(); ) { String condition = (String)((Iterator)???).next();
	        if (!condition.equals(""))
	        {
	          if (condition.indexOf(',') > 1) {
	            String[] cons = condition.split(":");
	            String fieldName = cons[0];
	            String[] values = cons[1].split(",");
	            String newCondition = null;
	            for (String value : values) {
	              if (newCondition == null)
	                newCondition = fieldName + ":" + value;
	              else {
	                newCondition = newCondition + " OR " + fieldName + ":" + value;
	              }
	            }
	            query.addFilterQuery(new String[] { newCondition });
	          } else {
	            query.addFilterQuery(new String[] { condition });
	          }
	        }
	      }
	    }

	    if ("true".equalsIgnoreCase(Configuration.getConfig("Weight"))) {
	      query.set("defType", new String[] { "edismax" });
	      String weightField = Configuration.getConfig("WeightField");
	      String[] weightFields = weightField.split(",");
	      query.set("qf", weightFields);
	    }

	    query.set("carrot.title", new String[] { Configuration.getConfig("clusteringTitle") });
	    query.set("carrot.url", new String[] { "ID" });

	    query.set("defType", new String[] { "edismax" });
	    String weightField = Configuration.getConfig("clusteringWeight");
	    String[] weightFields = weightField.split(",");
	    query.set("qf", weightFields);

	    if ((text == null) || (text.equals("")))
	      query.setQuery("*:*");
	    else {
	      query.setQuery(text);
	    }

	    String jsonStr = null;
	    try {
	      logger.debug(query);
	      QueryResponse sr = this.hss.query(query, SolrRequest.METHOD.POST);

	      JsonConfig jsonConfig = new JsonConfig();
	      jsonConfig.registerJsonValueProcessor(Date.class, new JsonValueProcessor() {
	        private SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	        public Object processObjectValue(String key, Object value, JsonConfig jsonConfig) { return value == null ? "" : this.sd.format(value); }

	        public Object processArrayValue(Object value, JsonConfig jsonConfig) {
	          return null;
	        }
	      });
	      SolrDocumentList results = sr.getResults();
	      JSONArray j = JSONArray.fromObject(results, jsonConfig);

	      NamedList resps = sr.getResponse();

	      Object a = resps.getVal(2);

	      List clusteringResult = (List)a;

	      List clusteringList = new ArrayList();

	      for (int i = 0; i < clusteringResult.size(); i++) {
	        Object o = clusteringResult.get(i);
	        SimpleOrderedMap label = (SimpleOrderedMap)o;
	        String title = (String)((List)label.get("labels")).get(0);
	        if (!title.equals("Other Topics"))
	        {
	          title = removerepeatedchar(title);
	          Map docs = new HashMap();
	          docs.put(title, (List)label.get("docs"));

	          clusteringList.add(docs);
	        }
	      }

	      JSONArray clustering = JSONArray.fromObject(clusteringList);

	      jsonStr = "{\"numFound\":\"" + 
	        results.getNumFound() + "\"," + 
	        "\"start\":\"" + results.getStart() + "\"," + 
	        "\"maxScore\":\"" + results.getMaxScore() + "\"," + 
	        "\"docs\":" + j.toString() + "," + 
	        "\"clustering\":" + clustering.toString() + 
	        "}";
	    }
	    catch (SolrServerException e) {
	      logger.error(query);
	      logger.error("搜索时出错：" + e.getMessage());
	      throw new ServiceException("搜索时出错：" + e.getMessage());
	    } catch (Exception e) {
	      logger.error(query);
	      logger.error("搜索时出错：" + e.getMessage());
	      throw new ServiceException("搜索时出错：" + e.getMessage());
	    }

	    return jsonStr;
	  }

	  private String removerepeatedchar(String str)
	  {
	    List data = new ArrayList();
	    char b;
	    for (int i = 0; i < str.length(); i++) {
	      String s = str.substring(i, i + 1);
	      b = str.charAt(i);
	      if (b <= 'ÿ') {
	        data.add(String.valueOf(b));
	      }
	      else if (!data.contains(s)) {
	        data.add(s);
	      }
	    }

	    String result = "";
	    for (String s : data) {
	      result = result + s;
	    }

	    return result;
	  }

	  public String search(String databaseName, String category, List<String> conditions, String text, int pageSize, int pageNo, String sortField, boolean descending)
	    throws ServiceException
	  {
	    String[] str = { "+", "-", "&", "|", "!", "(", ")", "{", "}", "[", "]", "^", "\"", "~", "*", "?", ":", "\"" };
	    for (String s : str) {
	      text = text.replaceAll("\\" + s, "\\\\" + s);
	    }

	    List newConditions = escape(conditions);
	    conditions = newConditions;

	    SolrQuery query = new SolrQuery();
	    query.setStart(Integer.valueOf((pageNo - 1) * pageSize));
	    query.setRows(Integer.valueOf(pageSize));
	    query.setFields(new String[] { "*,score" });

	    if ((databaseName != null) && (!databaseName.equals(""))) {
	      String newCondition = createNewCondition(databaseName);
	      query.addFilterQuery(new String[] { "database:" + newCondition });
	    }

	    if ((category != null) && (!category.equals("")))
	      query.addFilterQuery(new String[] { "catetory:" + category });
	    String fieldName;
	    String[] values;
	    String newCondition;
	    if (conditions != null) {
	      for (??? = conditions.iterator(); ((Iterator)???).hasNext(); ) { String condition = (String)((Iterator)???).next();
	        if (!condition.equals(""))
	        {
	          if (condition.indexOf(',') > 1) {
	            String[] cons = condition.split(":");
	            fieldName = cons[0];
	            values = cons[1].split(",");
	            newCondition = null;
	            for (String value : values) {
	              if (newCondition == null)
	                newCondition = fieldName + ":" + value;
	              else {
	                newCondition = newCondition + " OR " + fieldName + ":" + value;
	              }
	            }
	            query.addFilterQuery(new String[] { newCondition });
	          } else {
	            query.addFilterQuery(new String[] { condition });
	          }
	        }
	      }
	    }

	    if ((text == null) || (text.equals(""))) {
	      query.setQuery("*:*");
	    }
	    else if (text.matches("^[a-zA-Z]*"))
	    {
	      logger.debug("search,搜索词全是英文字母，启动拼音搜索");
	      query.setQuery("pinyin:" + text);
	    } else {
	      query.setQuery(text);
	    }

	    String sortFieldName = "score";
	    if ((sortField != null) && (!sortField.equals(""))) {
	      sortFieldName = sortField;
	    }
	    if (descending)
	      query.setSort(sortFieldName, SolrQuery.ORDER.desc);
	    else {
	      query.setSort(sortFieldName, SolrQuery.ORDER.asc);
	    }

	    if ("true".equalsIgnoreCase(Configuration.getConfig("Weight"))) {
	      query.set("defType", new String[] { "edismax" });
	      String weightField = Configuration.getConfig("WeightField");
	      String[] weightFields = weightField.split(",");
	      query.set("qf", weightFields);
	    }

	    if ((Configuration.getConfig("Highlight").equalsIgnoreCase("true")) && 
	      (Configuration.getConfig("HighlightField") != null) && 
	      (!Configuration.getConfig("HighlightField").equalsIgnoreCase("")))
	    {
	      query.setHighlight(true);
	      String[] fields = Configuration.getConfig("HighlightField").split(",");
	      values = (newCondition = fields).length; for (fieldName = 0; fieldName < values; fieldName++) { String field = newCondition[fieldName];
	        query.addHighlightField(field);
	      }

	    }

	    if ((Configuration.getConfig("Facet").equalsIgnoreCase("true")) && 
	      (Configuration.getConfig("FacetField") != null) && 
	      (!Configuration.getConfig("FacetField").equalsIgnoreCase("")))
	    {
	      query.setFacet(true);
	      String[] fields = Configuration.getConfig("FacetField").split(",");
	      values = (newCondition = fields).length; for (fieldName = 0; fieldName < values; fieldName++) { String field = newCondition[fieldName];
	        query.addFacetField(new String[] { field });
	      }

	    }

	    query.setTerms(true);

	    String jsonStr = null;
	    try {
	      logger.debug(query);
	      QueryResponse sr = this.hss.query(query, SolrRequest.METHOD.POST);

	      JsonConfig jsonConfig = new JsonConfig();
	      jsonConfig.registerJsonValueProcessor(Date.class, new JsonValueProcessor() {
	        private SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	        public Object processObjectValue(String key, Object value, JsonConfig jsonConfig) { return value == null ? "" : this.sd.format(value); }

	        public Object processArrayValue(Object value, JsonConfig jsonConfig) {
	          return null;
	        }
	      });
	      SolrDocumentList results = sr.getResults();
	      JSONArray j = JSONArray.fromObject(results, jsonConfig);

	      Map hls = sr.getHighlighting();
	      JSONArray jhls = JSONArray.fromObject(hls);

	      List facets = sr.getFacetFields();

	      JSONObject jfacets = facetsToJSONArray(facets);

	      jsonStr = "{\"numFound\":\"" + results.getNumFound() + "\"," + 
	        "\"start\":\"" + results.getStart() + "\"," + 
	        "\"maxScore\":\"" + results.getMaxScore() + "\"," + 
	        "\"docs\":" + j.toString() + "," + 
	        "\"highlight\":" + jhls.toString() + "," + 
	        "\"facet\":" + jfacets.toString() + 
	        "}";
	    }
	    catch (SolrServerException e)
	    {
	      logger.error(query);
	      logger.error("搜索时出错：" + e.getMessage());
	      throw new ServiceException("搜索时出错：" + e.getMessage());
	    } catch (Exception e) {
	      logger.error("搜索时出错：" + e.getMessage());
	      throw new ServiceException("搜索时出错：" + e.getMessage());
	    }

	    return jsonStr;
	  }

	  private JSONObject facetsToJSONArray(List<FacetField> facets)
	  {
	    Map facetMap = new HashMap();
	    for (FacetField f : facets)
	    {
	      String name = f.getName();
	      List values = f.getValues();
	      Map countMap = new HashMap();
	      for (FacetField.Count c : values)
	        if (c.getCount() != 0L)
	        {
	          String countName = c.getName();
	          String count = c.getCount();
	          countMap.put(countName, count);
	        }
	      facetMap.put(name, countMap);
	    }
	    JSONObject jfacets = JSONObject.fromObject(facetMap);
	    return jfacets;
	  }

	  public String mlt(String ID)
	    throws ServiceException
	  {
	    SolrQuery query = new SolrQuery();

	    if ((ID == null) || (ID.equals(""))) {
	      return null;
	    }
	    query.setQuery("ID:" + ID);

	    query.setParam("mlt", true);
	    query.setParam("mlt.fl", new String[] { "text" });
	    query.setParam("mlt.count", new String[] { Configuration.getConfig("MLTCount") });

	    String jsonStr = null;
	    try {
	      logger.debug(query);
	      QueryResponse sr = this.hss.query(query);

	      JsonConfig jsonConfig = new JsonConfig();
	      jsonConfig.registerJsonValueProcessor(Date.class, new JsonValueProcessor() {
	        private SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	        public Object processObjectValue(String key, Object value, JsonConfig jsonConfig) { return value == null ? "" : this.sd.format(value); }

	        public Object processArrayValue(Object value, JsonConfig jsonConfig) {
	          return null;
	        }
	      });
	      SolrDocumentList results = sr.getResults();
	      JSONArray j = JSONArray.fromObject(results, jsonConfig);

	      SimpleOrderedMap moreLikeThis = (SimpleOrderedMap)sr.getResponse().get("moreLikeThis");

	      SolrDocumentList mlts = (SolrDocumentList)moreLikeThis.get(ID);
	      mlts = mlts == null ? new SolrDocumentList() : mlts;
	      JSONArray jMlts = JSONArray.fromObject(mlts, jsonConfig);

	      jsonStr = "{\"numFound\":\"" + results.getNumFound() + "\"," + 
	        "\"start\":\"" + results.getStart() + "\"," + 
	        "\"maxScore\":\"" + results.getMaxScore() + "\"," + 
	        "\"docs\":" + j.toString() + "," + 
	        "\"mlts\":" + jMlts.toString() + 
	        "}";
	    }
	    catch (SolrServerException e) {
	      logger.error(query);
	      logger.error("搜索相似内容时出错：" + e.getMessage());
	      throw new ServiceException("搜索相似内容时出错：" + e.getMessage());
	    } catch (Exception e) {
	      logger.error(query);
	      logger.error("搜索相似内容时出错：" + e.getMessage());
	      throw new ServiceException("搜索相似内容时出错：" + e.getMessage());
	    }

	    return jsonStr;
	  }

	  public String spell(String databaseName, String text)
	    throws ServiceException
	  {
	    List words = new ArrayList();

	    SolrQuery query = new SolrQuery();
	    logger.debug(query);
	    query.setRequestHandler("/spell");

	    if ((databaseName != null) && (!databaseName.equals(""))) {
	      String newCondition = createNewCondition(databaseName);
	      query.addFilterQuery(new String[] { "database:" + newCondition });
	    }

	    query.setQuery(text);
	    try
	    {
	      QueryResponse sr = this.hss.query(query);
	      SpellCheckResponse spellResults = sr.getSpellCheckResponse();
	      List ss = spellResults.getSuggestions();

	      for (SpellCheckResponse.Suggestion s : ss) {
	        List word = s.getAlternatives();
	        words.addAll(word);
	      }
	    }
	    catch (SolrServerException e) {
	      logger.error(query);
	      logger.error("搜索词检查时出错：" + e.getMessage());
	      throw new ServiceException("搜索词检查时出错：" + e.getMessage());
	    }

	    JSONArray j = JSONArray.fromObject(words);
	    return j.toString();
	  }

	  public String suggest(String databaseName, String text) throws ServiceException
	  {
	    List words = new ArrayList();

	    SolrQuery query = new SolrQuery();
	    logger.debug(query);

	    if (text.matches("^[a-zA-Z]*"))
	    {
	      logger.debug("suggest,参数全是英文字母，启动拼音搜索提示");
	      query.setRequestHandler("/pinyinSuggest");
	    }
	    else {
	      query.setRequestHandler("/suggest");
	    }

	    if ((databaseName != null) && (!databaseName.equals(""))) {
	      String newCondition = createNewCondition(databaseName);
	      query.addFilterQuery(new String[] { "database:" + newCondition });
	    }

	    query.setQuery(text);
	    try
	    {
	      QueryResponse sr = this.hss.query(query);
	      SpellCheckResponse spellResults = sr.getSpellCheckResponse();
	      List ss = spellResults.getSuggestions();

	      for (SpellCheckResponse.Suggestion s : ss) {
	        List word = s.getAlternatives();
	        words.addAll(word);
	      }
	    }
	    catch (SolrServerException e) {
	      logger.error(query);
	      logger.error("搜索词提示时出错：" + e.getMessage());
	      throw new ServiceException("搜索词提示时出错：" + e.getMessage());
	    }

	    JSONArray j = JSONArray.fromObject(words);
	    return j.toString();
	  }

	  public List<String> escape(List<String> conditions) throws ServiceException
	  {
	    return conditions;
	  }
}
