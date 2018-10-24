package com.example.demo;

import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping(value = "/employees")
class EmployeeController {
    private final EmployeeRepository repository;
    EmployeeController(EmployeeRepository repository){
        this.repository = repository;
    }

    @GetMapping()
    Resources<Resource<Employee>> all(){
        List<Resource<Employee>> employees = repository.findAll().stream()
                .map(employee -> new Resource<>(employee,
                        linkTo(methodOn(EmployeeController.class).one(employee.getId())).withSelfRel(),
                        linkTo(methodOn(EmployeeController.class).all()).withRel("employees")))
                .collect(Collectors.toList());
        return new Resources<>(employees,
                linkTo(methodOn(EmployeeController.class).all()).withSelfRel());
    }

    @PostMapping()
    Employee newEmployee(@RequestBody Employee newEmployee){
        return  repository.save(newEmployee);
    }

    @GetMapping("/{id}")
    Resource<Employee> one(@PathVariable Long id) {
        Employee employee =  repository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return new Resource<>(employee,
                linkTo(methodOn(EmployeeController.class).one(id)).withSelfRel(),
                linkTo(methodOn(EmployeeController.class).all()).withRel("employees"));
    }


    @PutMapping("/{id}")
    Employee replaceEmployee(@RequestBody Employee newEmp, @PathVariable Long id){
        return repository.findById(id)
                .map(employee -> {
                    employee.setName(newEmp.getName());
                    employee.setRole(newEmp.getRole());
                    return repository.save(employee);
                })
                .orElseGet(()->{
                   newEmp.setId(id);
                   return repository.save(newEmp);
                });
    }

    @DeleteMapping("/{id}")
    void deleteEmployee(@PathVariable Long id){
        repository.deleteById(id);
    }
}
