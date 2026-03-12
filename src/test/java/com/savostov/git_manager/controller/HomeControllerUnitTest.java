package com.savostov.git_manager.controller;

import com.savostov.git_manager.model.Repo;
import com.savostov.git_manager.model.User;
import com.savostov.git_manager.repository.UserRepository;
import com.savostov.git_manager.service.RepositoryService;
import com.savostov.git_manager.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class HomeControllerUnitTest {

    @AfterEach
    void cleanupSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void editRepositoryForm_whenOwner_returnsEditViewAndPutsRepoInModel() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", "n/a")
        );

        User owner = new User();
        owner.setUsername("alice");
        Repo repo = new Repo();
        repo.setId(5L);
        repo.setOwner(owner);
        repo.setName("r1");

        StubRepositoryService repositoryService = new StubRepositoryService();
        repositoryService.put(repo);

        HomeController controller = new HomeController();
        inject(controller, "repositoryService", repositoryService);
        inject(controller, "userService", new UserService());
        inject(controller, "userRepository", dummyUserRepository());

        Model model = new ExtendedModelMap();
        String view = controller.editRepositoryForm(5L, model);

        assertThat(view).isEqualTo("repository_edit");
        assertThat(model.getAttribute("repo")).isSameAs(repo);
    }

    @Test
    void editRepositoryForm_whenNotOwner_redirectsHome() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", "n/a")
        );

        User owner = new User();
        owner.setUsername("bob");
        Repo repo = new Repo();
        repo.setId(5L);
        repo.setOwner(owner);

        StubRepositoryService repositoryService = new StubRepositoryService();
        repositoryService.put(repo);

        HomeController controller = new HomeController();
        inject(controller, "repositoryService", repositoryService);
        inject(controller, "userService", new UserService());
        inject(controller, "userRepository", dummyUserRepository());

        Model model = new ExtendedModelMap();
        String view = controller.editRepositoryForm(5L, model);

        assertThat(view).isEqualTo("redirect:/home");
    }

    @Test
    void deleteRepository_whenOwner_deletesAndRedirectsHome() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", "n/a")
        );

        User owner = new User();
        owner.setUsername("alice");
        Repo repo = new Repo();
        repo.setId(7L);
        repo.setOwner(owner);

        StubRepositoryService repositoryService = new StubRepositoryService();
        repositoryService.put(repo);

        HomeController controller = new HomeController();
        inject(controller, "repositoryService", repositoryService);
        inject(controller, "userService", new UserService());
        inject(controller, "userRepository", dummyUserRepository());

        String view = controller.deleteRepository(7L);

        assertThat(view).isEqualTo("redirect:/home");
        assertThat(repositoryService.deletedIds).contains(7L);
    }

    @Test
    void deleteRepository_whenNotOwner_doesNotDelete() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", "n/a")
        );

        User owner = new User();
        owner.setUsername("bob");
        Repo repo = new Repo();
        repo.setId(7L);
        repo.setOwner(owner);

        StubRepositoryService repositoryService = new StubRepositoryService();
        repositoryService.put(repo);

        HomeController controller = new HomeController();
        inject(controller, "repositoryService", repositoryService);
        inject(controller, "userService", new UserService());
        inject(controller, "userRepository", dummyUserRepository());

        String view = controller.deleteRepository(7L);

        assertThat(view).isEqualTo("redirect:/home");
        assertThat(repositoryService.deletedIds).doesNotContain(7L);
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static UserRepository dummyUserRepository() {
        return (UserRepository) java.lang.reflect.Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    Class<?> rt = method.getReturnType();
                    if (rt.equals(boolean.class)) return false;
                    if (rt.equals(int.class)) return 0;
                    if (rt.equals(long.class)) return 0L;
                    return null;
                }
        );
    }

    static class StubRepositoryService extends RepositoryService {
        private final Map<Long, Repo> repos = new HashMap<>();
        private final Set<Long> deletedIds = new HashSet<>();

        void put(Repo repo) {
            repos.put(repo.getId(), repo);
        }

        @Override
        public Repo getRepositoryById(Long id) {
            return repos.get(id);
        }

        @Override
        public void deleteRepository(Long id) {
            deletedIds.add(id);
        }
    }
}

