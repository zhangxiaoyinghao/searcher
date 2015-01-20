/*
 * Copyright © 2010-2013 by Beijing VideoWorks Technology Co., Ltd. All rights reserved.
 * /

/* @(#) ExampleController.java
 * Project: vicmmam
 * Package: cn.videoworks.vicmmam.web
 * Author:  Archetype Generate
 */
package cn.chinattclub.searcher.web;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.zy.commons.webdev.constant.ResponseStatusCode;
import cn.zy.commons.webdev.http.RestResponse;




@Controller
@RequestMapping("searcher/test")
public class TestController {
	
	private static final Logger log = LoggerFactory.getLogger(TestController.class);

	@RequestMapping(value="test",method=RequestMethod.GET)
	@ResponseBody
	public RestResponse test(String params) {
		RestResponse restResponse = new RestResponse();
		log.debug("test"+params);
		restResponse.setMessage("中文");
		restResponse.setStatusCode(ResponseStatusCode.OK);
		return restResponse;
	}
	
}
