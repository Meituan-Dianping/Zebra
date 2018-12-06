package com.dianping.zebra.administrator.controller.base;

import com.dianping.zebra.administrator.controller.AbstractController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Created by tong.xin on 18/1/19.
 */

@RestController
@RequestMapping(value = "/home")
public class HomeController extends AbstractController {

	@RequestMapping(method = RequestMethod.GET)
	public void index() {
	}

}
