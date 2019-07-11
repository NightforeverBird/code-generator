package pg.laziji.generator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pg.laziji.generator.model.TableItem;
import pg.laziji.generator.service.GeneratorService;

import javax.annotation.Resource;

@ActiveProfiles("example")
@RunWith(SpringRunner.class)
@SpringBootTest
public class ExampleTest {

    @Resource
    private GeneratorService generatorService;

    @Test
    public void test() {
        generatorService.generateZip(new TableItem[]{
                new TableItem("SYS_USER"),
                new TableItem("SYS_USER_INFO")
        }, "D:/A_GENERATOR/code.zip");
    }
}
