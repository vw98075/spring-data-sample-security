package com.example;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@SpringBootApplication
public class SpringDataSampleApplication {

	@Bean
	CommandLineRunner initData(BookRepository bookRepository, AuthorRepository authorRepository){
		return args -> {
			SecurityContext securityContext = new SecurityContextImpl();

			final Properties users = new Properties();
			users.put("anonymous","secret,ROLE_ADMIN,enabled");
			InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager(users);

			Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
			grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
			AnonymousAuthenticationToken anonymousAuthenticationToken = new AnonymousAuthenticationToken("data-init", manager.loadUserByUsername("anonymous"), grantedAuthorities);
			securityContext.setAuthentication(anonymousAuthenticationToken);
			SecurityContextHolder.setContext(securityContext);

			bookRepository.save(new Book("Spring Microservices", "Learn how to efficiently build and implement microservices in Spring,\n" +
						"and how to use Docker and Mesos to push the boundaries. Examine a number of real-world use cases and hands-on code examples.\n" +
						"Distribute your microservices in a completely new way", LocalDate.of(2016, 06, 28), new Money(new BigDecimal(45.83)),
						Arrays.asList(authorRepository.save(new Author("Felipe", "Gutierrez")))));
			bookRepository.save(new Book("Pro Spring Boot", "A no-nonsense guide containing case studies and best practise for Spring Boot",
						LocalDate.of(2016, 05, 21 ), new Money(new BigDecimal(42.74)),
						Arrays.asList(authorRepository.save(new Author("Rajesh", "RV")))));

			SecurityContextHolder.clearContext();
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringDataSampleApplication.class, args);
	}
}

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	/**
	 * This section defines the user accounts which can be used for
	 * authentication as well as the roles each user has.
	 */
	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {

		auth.inMemoryAuthentication()
				.withUser("joe").password("secret").roles("USER").and()
				.withUser("jane").password("secret").roles("USER", "ADMIN");
	}

	/**
	 * This section defines the security policy for the app.
	 * - BASIC authentication is supported (enough for this REST-based demo)
	 * - /employees is secured using URL security shown below
	 * - CSRF headers are disabled since we are only testing the REST interface,
	 *   not a web one.
	 *
	 * NOTE: GET is not shown which defaults to permitted.
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http
				.httpBasic().and()
				.authorizeRequests()
				.antMatchers(HttpMethod.POST, "/books").hasRole("ADMIN")
				.antMatchers(HttpMethod.PUT, "/books/**").hasRole("ADMIN")
				.antMatchers(HttpMethod.PATCH, "/books/**").hasRole("ADMIN")
				.antMatchers(HttpMethod.POST, "/authors").hasRole("ADMIN")
				.antMatchers(HttpMethod.PUT, "/authors/**").hasRole("ADMIN")
				.antMatchers(HttpMethod.PATCH, "/authors/**").hasRole("ADMIN").and()
				.csrf().disable();
	}
}

@Data
@Entity
@NoArgsConstructor
class Book {

	@Id
	@GeneratedValue
	private Long id;

	@Size(min=1, max=255)
	private String title;

	@Size(min=1, max=255)
	private String description;

	@NotNull
	private LocalDate publishedDate;

	@NotNull
	@Embedded
	private Money price;

	@Size(min = 1)
	@ManyToMany
	private List<Author> authors;

	Book(String title, String description, LocalDate publishedDate, Money price, List<Author> authors) {
		this.title = title;
		this.description = description;
		this.publishedDate = publishedDate;
		this.price = price;
		this.authors = authors;
	}
}

@Embeddable
@Data
@NoArgsConstructor
class Money {

	enum Currency {CAD, EUR, USD }

	@DecimalMin(value="0",inclusive=false)
	@Digits(integer=1000000000,fraction=2)
	private BigDecimal amount;

	private Currency currency;

	Money(BigDecimal amount){
		this(Currency.USD, amount);
	}

	Money(Currency currency, BigDecimal amount){
		this.currency = currency;
		this.amount = amount;
	}
}

@PreAuthorize("hasRole('USER')")
@RepositoryRestResource
interface  BookRepository extends CrudRepository<Book, Long> {

	@PreAuthorize("hasRole('ADMIN')")
	@Override
	Book save(Book book);

	@PreAuthorize("hasRole('ADMIN')")
	@Override
	void delete(Long aLong);

	List<Book> findByTitle(@Param("title") String title);
	List<Book> findByTitleContains(@Param("keyword") String keyword);
	List<Book> findByPublishedDateAfter(@Param("publishedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishedDate);
	List<Book> findByTitleContainsAndPublishedDateAfter(@Param("keyword") String keyword,
														@Param("publishedDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishedDate);
	List<Book> findByTitleContainsAndPriceCurrencyAndPriceAmountBetween(@Param("keyword") String keyword,
																		@Param("currency") Money.Currency currency,
																		@Param("low") BigDecimal low,
																		@Param("high") BigDecimal high);
	List<Book> findByAuthorsLastName(@Param("lastName") String lastName);
}

@Entity
@Data
@NoArgsConstructor
class Author {

	@Id
	@GeneratedValue
	private Long id;

	@Size(min = 1, max=255)
	private String firstName;

	@Size(min = 1, max = 255)
	private String lastName;

	@Size(min = 1)
	@ManyToMany(mappedBy = "authors")
	private List<Book> books;

	Author(String firstName, String lastName){
		this.firstName = firstName;
		this.lastName = lastName;
	}
}

@PreAuthorize("hasRole('USER')")
@RepositoryRestResource
interface AuthorRepository extends CrudRepository<Author, Long>{

	@PreAuthorize("hasRole('ADMIN')")
	@Override
	Author save(Author author);

	@PreAuthorize("hasRole('ADMIN')")
	@Override
	void delete(Long aLong);

	List<Author> findByLastName(@Param("lastName") String lastName);
	List<Author> findByBooksTitle(@Param("title") String title);
}