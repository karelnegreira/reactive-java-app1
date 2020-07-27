package com.example.springreactive2;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.function.DatabaseClient;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringReactive2Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringReactive2Application.class, args);
	}

}

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("users")
class User {
	private Integer id;
	private String firstname;
	private String lastname;
}

@Repository
class UserRepository {
	private DatabaseClient databaseClient;

	public UserRepository(DatabaseClient client) {
		this.databaseClient = client;
	}

	public Flux<User> findAll() {
		return databaseClient.select().from("users").as(User.class).fetch().all();
	}

	public Mono<User> findOne(Integer id) {
		return databaseClient.execute().sql("select * from users where users.id = $1")
				.bind("$1", id)
				.as(User.class)
				.fetch()
				.one();
	}

	public Mono<Void> deleteAll() {
		return databaseClient.execute().sql("delete from users").then();
	}

	public Mono<Void> save(User user) {
		return databaseClient.insert()
				.into(User.class).table("users")
				.using(user)
				.then();
	}

	public Mono<Void> init() {
		return databaseClient.execute().sql("CREATE TABLE...").then()
				.then(deleteAll())
				.then(save(new User(1, "dffgfgfgf", "gfgdfgd")))
				.then(save(new User(2, "fdfdfdfdf", "ytyrtyrt")))
				.then(save(new User(3, "dfgdgdfgd", "gdfgdfgd")));
	}
}

@RestController
class UserController {
	private UserRepository repository;

	public UserController(UserRepository repository) {
		this.repository = repository;
	}

	@GetMapping("/users")
	public Flux<User> findAll() {
		return repository.findAll();
	}

	@GetMapping("/users/{id}")
	public Mono<User> findOne(@PathVariable("id") Integer id) {
		return repository.findOne(id);
	}

	@PostMapping("/users/save")
	public Mono<Void> save(User user) {
		return repository.save(user);
	}

	@DeleteMapping("/users")
	public Mono<Void> deleteAll() {
		return repository.deleteAll();
	}

}

@Configuration
@EnableR2dbcRepositories
class PostgresDbConfiguration extends AbstractR2dbcConfiguration {

	@Override
	public ConnectionFactory connectionFactory() {
		PostgresqlConnectionConfiguration config = PostgresqlConnectionConfiguration.builder()
				.host("localhost")
				.database("test2")
				.username("postgres")
				.password("admin")
				.build();

		return new PostgresqlConnectionFactory(config);
	}
}
