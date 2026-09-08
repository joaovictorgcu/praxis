package school.cesar.praxis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = "praxis.dados-exemplo=false")
class PraxisApplicationTests {

	@Test
	void contextLoads() {
	}

}
