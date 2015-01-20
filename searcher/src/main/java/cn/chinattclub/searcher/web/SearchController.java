package cn.chinattclub.searcher.web;

/**
 * TODO Put here a description of what this class does.
 *
 * @author zhangying.
 *         Created 2015-1-20.
 */
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.chinattclub.searcher.dto.SearchParam;
import cn.zy.commons.webdev.http.RestResponse;


@Controller
@RequestMapping("searcher/")
public class SearchController
{
  private static Log logger = LogFactory.getLog(SearchController.class.getName());

  @RequestMapping(value="test",method=RequestMethod.GET)
  @ResponseBody
  public RestResponse search(SearchParam sp) {
	  String result = null;
  RestResponse restResponse = new RestResponse();
    try
    {
      if ((sp.getPageSize() < 1) || (sp.getPageNo() < 1)) {
        logger.error("页码或页号不正确");
      }
      SearcherEngine se = SearcherFactory.createSearcher();
      result = se.search(sp.getDatabase(), sp.getCategory(), sp.getConditions(), sp.getText(), sp.getPageSize(), sp.getPageNo(), sp.getSortField(), sp.isDescending());

      Map tmp = (Map)JsonConverter.parse(result, Map.class);
      tmp.put("statusCode", "0");
      tmp.put("message", "操作成功");

      List synonyms = Synonyms.getSynonyms(sp.getText());
      tmp.put("synonyms", synonyms);

      JSONObject jsonResult = JSONObject.fromObject(tmp);
      result = jsonResult.toString();
    }
    catch (ServiceException e) {
      result = "{\"statusCode\":\"1\",\"message\":\"执行搜索时出现错误：" + e.getMessage() + "\"}";
      logger.error("执行搜索时出现错误：" + e.getMessage());
      e.printStackTrace();
    }

    return result;
    } 
}
