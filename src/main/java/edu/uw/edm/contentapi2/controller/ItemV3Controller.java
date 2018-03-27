package edu.uw.edm.contentapi2.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.uw.edm.contentapi2.controller.model.Document;

/**
 * @author Maxime Deravet
 * Date: 3/27/18
 */
@RestController
@RequestMapping("/v3/item")
public class ItemV3Controller {



    @RequestMapping("/{itemId}")
    public Document getItem(@PathVariable("itemId")String itemId) {

        Document document = new Document();
        document.setId(itemId);

        return document;
    }
}
