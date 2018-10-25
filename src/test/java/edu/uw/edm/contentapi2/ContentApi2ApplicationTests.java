package edu.uw.edm.contentapi2;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import edu.uw.edm.contentapi2.common.FieldMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ContentApi2ApplicationTests {

	@MockBean
	private FieldMapper fieldMapper;
	@Test
	public void contextLoads() {
	}

}
