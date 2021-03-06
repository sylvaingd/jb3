package im.bci.jb3;

import freemarker.template.TemplateModelException;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.filter.CharacterEncodingFilter;

@Configuration
@EnableAutoConfiguration
@EnableAsync
@ComponentScan
@EnableSpringDataWebSupport
@EnableScheduling
public class Jb3Application implements CommandLineRunner {

    @Value("${jb3.room.default}")
    private String defaultRoom;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("jb3 or not to be!");
    }

    @Autowired
    public void setupFreemarker(freemarker.template.Configuration conf) throws TemplateModelException {
        conf.setDateTimeFormat("yyyy/MM/dd#HH:mm:ss");
        conf.setTimeZone(TimeZone.getTimeZone("UTC"));
        conf.setSharedVariable("jb3DefaultRoom", defaultRoom);
    }

    @Bean(name = "botExecutor")
    public ThreadPoolTaskExecutor botExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(1);
        return executor;
    }

    @Bean
    CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Jb3Application.class, args);
    }
}
