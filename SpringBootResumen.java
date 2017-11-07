// Activar escaneo de componentes y autoconfiguracion con @SpringBootApplication
// Combina @Configuration + @ComponentScan + @EnableAutoConfiguration
@SpringBootApplication
public class FooApplication {
  public static void main(String[] args) {
    // Iniciar la aplicación
    SpringApplication.run(FooApplication.class, args);
  }
}

// @Configuration:  Marca una clase como clase de configuración
// @ComponentScan:  Activa el escaneo de componentes para que las clases controller puedan 
//					registrarse automaticamente como beans en el contexto de la aplicacion
// @EnableAutoConfiguration: Configuración basada en depedendencias

// Build e iniciar
gradle bootRun
// O
gradle build
gradle -jar build/libs/FooApplication-0.0.1-SNAPSHOT.jar

// Testing en Spring Boot
@RunWith(SpringJUnit4ClassRunner.class)
// cargar contexto en Spring Boot
@SpringApplicationConfiguration(classes = FooApplication.class)
@WebAppConfiguration
public class FooApplicationTests {
  // Probar que el contexto se ha cargado -> No está implementado, el test falla si el contexto no se está cargado
  @Test
  public void cargaContexto() {
  }
}

@SpringBootTest
class MySpec extends Specification {
    
}

// application.properties es opcional
// configurar el puerto del tomcat 8080
server.port=8081


//Inyectar las dependencias en el constructor del controlador para ver los componenetes dependientes y facilitar el testing
@Controller
@RequestMapping("/")
public class UserController {

  private UserRepository userRepository;

  @Autowired
  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }
}


//Definir condiciones que comprueban si JdbcTemplate está disponible en el classpath

//El sistema de configuracion de spring boot usa Condition
//Hay varias clases de configuracion en spring-boot-autoconfigure.jar
//que modifican la configuración si se dan determinadas condiciones
public class JdbcTemplateCondition implements Condition {
  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    try {
      context.getClassLoader().loadClass("org.springframework.jdbc.core.JdbcTemplate");
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}

// Usar una condicion personalizada para que se instancie o no un Bean
@Conditional(JdbcTemplateCondition.class)
public class MyService {
  ...
}

// Sobreescribir la autoconfiguracion por ejemplo Security
// Tiene que haber una clase de configuracion especifica en el classpath
// Spring se salta la autoconfiguracion y usa la personalizada
// Esta clase tiene que extendida y anotada con @Configuration
// Ademas tiene que tener @EnableWebSecurity para activar Spring Security

// La lista con las clases de autoconfiguracion
spring-boot-autoconfigure.jar -> spring.factories

// Generar informe del arranque de la aplicación en la consola
// parametro de la VM
-Ddebug

// o en application.properties
debug=true

// Test de integracion cargando el contexto
// Para hacer un test de integración todos los componentes tienen que estar levantados
// Para hacerlo automaticamente se usa SpringJUnit4ClassRunner.
// Ayuda con la carga del contexto de Spring para tests con JUnit
// Usando @ContextConfiguration no se aplica application.properties y logging
// @ContextConfiguration especifica como cargar el contexto: Se pasa como parametro una clase de configuración
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=PlaylistConfiguration.class)
public class ColoresServiceTests {

  @Autowired
  private ColoresService coloresService;

  @Test
  public void testService() {
    ColoresService colores = coloresService.findByName("rojo");
    assertEquals("255.255.255", colores.getRGB());
    assertEquals("#FF0000", colores.getHex());
  }
}

// Test de integracion con contexto + properties + logging
// Cambiar @ContextConfiguration por @SpringApplicationConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes=PlaylistConfiguration.class)
public class ColoresServiceTests {
  ...
}

//Test de controller 
//
// - Con mock y sin servidor de aplicaciones
// - Ejecutando los test en el servidor de aplicaciones

// Test de controller con Mock de Spring MVC
//
// Instanciar un MockMvc con MockMvcBuilders
// standaloneSetup()     - Instancia un Mock MVC para servirlos a uno o varios controllers, de forma 
//							que que las instancias se crean manualmente. Es como un test unitario para 
//							cada test en un controller.
//
// webAppContextSetup()  - Instancia un Mock MVC usando el contexto de la aplicacion, que incluye uno
//							o varios controladores usando una instancia de WebApplicationContext.
//							La clase de test tiene que estar anotada con WebAppConfiguration para 
//							declarar que el contexto creado por el SpringJUnit4ClassRunner debe ser el WebApplicationContext. 
//							El webAppContextSetup() recibe un WebApplicationContext como parametro.
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = FooApplication.class)
@WebAppConfiguration
public class MockMvcWebTests {
  @Autowired
  private WebApplicationContext webContext;

  private MockMvc mockMvc;

  @Before
  public void setupMockMvc() {
    mockMvc = MockMvcBuilders
      .webAppContextSetup(webContext)
      .build();
  }

  @Test
  public void colores() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/colores"))
      .andExpect(MockMvcResultMatchers.status().isOk())
      .andExpect(MockMvcResultMatchers.view().name("colores"))
      .andExpect(MockMvcResultMatchers.model().attributeExists("basicos"))
      .andExpect(MockMvcResultMatchers.model().attribute("basicos",
      Matchers.is(Matchers.empty())));
  }
}

// colores() puede reescribirse con imports estaticos
@Test
public void colores() throws Exception {
  mockMvc.perform(get("/colores"))
    .andExpect(status().isOk())
    .andExpect(view().name("colores"))
    .andExpect(model().attributeExists("basicos"))
    .andExpect(model().attribute("basicos", is(empty())));
}

// Probar metodo con petición HTTP POST
@Test
public void postColor() throws Exception {
  mockMvc.perform(post("/colores"))
    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
    .param("nombre", "amarillo")
    .param("RGB", "#A00FF0")
    .andExpect(status().is3xxRedirection())
    .andExpect(header().string("Location", "/colores"));

  // Crear un color
  Color elColor = new Color();
  elColor.setId(1L);
  elColor.setNombre("amarillo");
  elColor.setRGB("#A00FF0");

  // Comprobar si hay un color nuevo
  mockMvc.perform(get("/colores"))
    .andExpect(status().isOk())
    .andExpect(view().name("colores"))
    .andExpect(model().attributeExists("basicos"))
    .andExpect(model().attribute("basicos", hasSize(1)))
    .andExpect(model().attribute("basicos",
    contains(samePropertyValuesAs(elColor))));
}

// Pruebas con spring security
// Añadir la dependencia de testCompile
testCompile("org.springframework.security:spring-security-test")

// Aplicar el configurador de Spring Security al crear la instancia MockMvc
// SecurityMockMvcConfigurers.springSecurity() - devuelve un Mock MVC que activa Spring Security para Mock MVC
@Before
public void setupMockMvc() {
  mockMvc = MockMvcBuilders
    .webAppContextSetup(webContext)
    .apply(springSecurity())
    .build();
}

// Test sin estar autenticado
@Test
public void unauthenticated() throws Exception() {
  mockMvc.perform(get("/"))
    .andExpect(status().is3xxRedirection())
    .andExpect(header().string("Location",
    "http://localhost/login"));
}


// Hay 2 maneras de usar un usuario autenticado en las pruebas
// @WithMockUser - Carga el contexto con un UserDetails y el nombre y pass dados
// @WithUserDetails - Carga el contexto buscando un UserDetails para el nombre de usuario dado
// Este UserDetails cargado se usa lo que dure el metodo

// Cortocircuitar la busqueda de un UserDetails y en vez de eso crear uno 
 Bypassing the normal lookup of a UserDetails object and instead create one
@Test
@WithMockUser(
  username="pepe",
  password="perez",
  roles="USER"
)
public void authenticatedUser() throws Exception {
  ...
}

// Using a real user from a UserDetailsService
@Test
@WithUserDetails("pepe")
public void authenticatedUser() throws Exception {
  ColorUsuario expectedColorUsuario = new ColorUsuario();
  expectedColorUsuario.setUsername("pepe");
  expectedColorUsuario.setPassword("perez");
  expectedColorUsuario.setFullname("Pepe Perez");

  mockMvc.perform(get("/"))
    .andExpect(status().isOk())
    .andExpect(view().name("colores"))
    .andExpect(model().attribute("usuario",
      samePropertyValuesAs(expectedColorUsuario)))
    .andExpect(model().attribute("basicos", hasSize(0)));
}


// Test con un servidor de aplicaciones (Tomcat)
// @WebIntegrationTest -> puede haber varios contextos de aplicacion, 
// pero para iniciar un contenedor de servlets hay que usar el RestTemplate 
// para realizar peticiones HTTP contra la aplicacion

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = coloresApplication.class)
@WebIntegrationTest
public class RealWebTest {

  @Test (expected=HttpClientErrorException.class)
  public void pageNotFound() {
    try {
      RestTemplate rest = new RestTemplate();
      // Perform GET request
      rest.getForObject("http://localhost:8080/qwerty", String.class);
      fail("Should result in HTTP 404");
    } catch (HttpClientErrorException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
      throw e;
    }
  }
}

//Iniciar el servidor en un puerto aleatorio "random=true" e inyectar su valor
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ColoresApplication.class)
@WebIntegrationTest(randomPort=true)
public class RealWebTest {

  @Value("${local.server.port}")
  private int port;

  @Test (expected=HttpClientErrorException.class)
  public void pageNotFound() {
    ...
    rest.getForObject("http://localhost:{port}/qwerty", String.class, port);
    ...
  }
}

// Test de front con Selenium
//Lo primero añadir la dependencia
testCompile("org.seleniumhq.selenium:selenium-java:2.52.0")

// Test con FirefoxDriver
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ColoresApplication.class)
@WebIntegrationTest(randomPort=true)
public class SeleniumWebTest {

  private static FirefoxDriver browser;

  @Value("${local.server.port}")
  private int port;

  @BeforeClass
  public static void openBrowser() {
    browser = new FirefoxDriver();
    browser.manage().timeouts()
      .implicitlyWait(10, TimeUnit.SECONDS);
  }

  @AfterClass
  public static void closeBrowser() {
    browser.quit();
  }

  @Test
  public void addColorABasicos() {
    String baseUrl = "http://localhost:" + port;

    browser.get(baseUrl);

    assertEquals("No hay colores en la lista de básicos",
    browser.findElementByTagName("div").getText());

    browser.findElementByName("nombre")
      .sendKeys("verde");
    browser.findElementByName("rgb")
      .sendKeys("#A00FF0");
    browser.findElementByTagName("form")
      .submit();

    WebElement dl = browser.findElementByCssSelector("dt.nombre");
    assertEquals("verde", dl.getText());

    WebElement dt = browser.findElementByCssSelector("dd.rgb");
    assertEquals("#A00FF0", dt.getText());
  }
}

// Ejecuta el código al inicio de la aplicación
@Component
public class MyListener implements ApplicationListener<ApplicationReadyEvent> {

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    // cosas();
  }
}


// Forzar un contexto limpio antes de cada test
@SpringBootTest
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class MiSpec extends Specification {
  def "foo"() {
    // cosas
  }
}

// Los eventos de aplicacion se suceden de la siguiente manera:
// 1 - ApplicationStartedEvent al iniciar, pero justo despues de registrar los listeners e inicializadores.
// 2 - ApplicationEnvironmentPreparedEvent al levantar el entorno, pero antes de crear el contexto.
// 3 - ApplicationPreparedEvent despues de cargar las definicones de beans, pero antes de recargar el contexto.
// 4 - ApplicationReadyEvent despues de la recarga y de que las llamadas relacionadas se procesen e indiquen que la aplicación está preparada.
// 5 - ApplicationFailedEvent cuando hay errores en el inicio
  
// Configurar el nivel de log mediante application.properties
@SpringBootApplication
@Slf4j
public class MyApp {
  public static void main(String[] args) {
    SpringApplication.run(MyApp.class, args);
    log.info("prueba de log");
  }
}

// application.properties
logging.level.com.example.MyApp=WARN

// Estructura recomendada para una aplicación SpringBoot
com
 +- example
     +- myproject
         +- Application.java
         |
         +- domain // Entities + Repos
         |   +- Customer.java
         |   +- CustomerRepository.java
         |
         +- service
         |   +- CustomerService.java
         |
         +- web
             +- CustomerController.java