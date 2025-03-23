-- Further refinement scaffs for specialized project templates
INSERT INTO scaffs (id, parent_id, name, descr, author) VALUES
('cccccccccccccccccccccccccccccccc', '66666666666666666666666666666666', 'Flask with SQLAlchemy', 'A Flask project with SQLAlchemy setup', 'user1'),
('dddddddddddddddddddddddddddddddd', '77777777777777777777777777777777', 'Django with DRF', 'Django project with Django Rest Framework', 'user2'),
('eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee', '88888888888888888888888888888888', 'Bootstrap with Vue', 'Bootstrap-based site with Vue.js integration', 'admin'),
('ffffffffffffffffffffffffffffffff', '99999999999999999999999999999999', 'C++ CLI with Argparse', 'C++ command-line app with argument parsing', 'user1'),
('abababababababababababababababab', 'cccccccccccccccccccccccccccccccc', 'Flask with Celery', 'Flask project integrated with Celery for task queuing', 'user1'),
('cdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcd', 'dddddddddddddddddddddddddddddddd', 'Django with GraphQL', 'Django project with GraphQL API', 'user2'),
('efefefefefefefefefefefefefefefef', 'eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee', 'Bootstrap with Tailwind', 'Bootstrap-based site with Tailwind CSS integration', 'admin'),
('ghghghghghghghghghghghghghghghgh', 'ffffffffffffffffffffffffffffffff', 'C++ CLI with Multithreading', 'C++ command-line app with multithreading support', 'user1');

-- Additional template file insertions for specialized projects
INSERT INTO insertion (scaff_id, filepath, value) VALUES
('cccccccccccccccccccccccccccccccc', 'models.py', 'from flask_sqlalchemy import SQLAlchemy
db = SQLAlchemy()
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    <<<additional_fields>>>'),
('dddddddddddddddddddddddddddddddd', 'serializers.py', 'from rest_framework import serializers
class UserSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = ["id", "username", "email"]
    <<<extra_serializer_fields>>>'),
('eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee', 'src/app.js', 'import Vue from "vue";
new Vue({
    el: "#app",
    data: {
        message: "<<<welcome_message>>>"
    }
});'),
('ffffffffffffffffffffffffffffffff', 'src/argparse.cpp', '#include <iostream>
#include <cstdlib>
int main(int argc, char** argv) {
    std::cout << "Command-line arguments parsed" << std::endl;
    <<<argparse_logic>>>
    return 0;
}'),
('abababababababababababababababab', 'tasks.py', 'from celery import Celery
app = Celery("tasks", broker="redis://localhost:6379/0")
@app.task
def add(x, y):
    return x + y
<<<celery_tasks>>>'),
('cdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcd', 'schema.py', 'import graphene
class Query(graphene.ObjectType):
    hello = graphene.String(default_value="Hello, world!")
schema = graphene.Schema(query=Query)
<<<graphql_definitions>>>'),
('efefefefefefefefefefefefefefefef', 'src/styles.css', '@tailwind base;
@tailwind components;
@tailwind utilities;
<<<extra_styles>>>'),
('ghghghghghghghghghghghghghghghgh', 'src/threading.cpp', '#include <thread>
void task() { /* Do something */ }
int main() {
    std::thread t1(task);
    t1.join();
    return 0;
}<<<threading_logic>>>');

-- Additional variable substitutions
INSERT INTO substitution (scaff_id, variable, value) VALUES
('cccccccccccccccccccccccccccccccc', 'additional_fields', 'email = db.Column(db.String(120), unique=True, nullable=False)'),
('dddddddddddddddddddddddddddddddd', 'extra_serializer_fields', 'password = serializers.CharField(write_only=True)'),
('eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee', 'welcome_message', '"Welcome to My Vue-Powered Bootstrap Site"'),
('ffffffffffffffffffffffffffffffff', 'argparse_logic', 'for (int i = 1; i < argc; ++i) { std::cout << "Arg " << i << ": " << argv[i] << std::endl; }'),
('abababababababababababababababab', 'celery_tasks', 'def multiply(x, y):
    return x * y'),
('cdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcd', 'graphql_definitions', 'class UserType(graphene.ObjectType):
    id = graphene.Int()
    username = graphene.String()'),
('efefefefefefefefefefefefefefefef', 'extra_styles', 'body { font-family: Arial, sans-serif; }'),
('ghghghghghghghghghghghghghghghgh', 'threading_logic', 'std::thread t2(task);
t2.join();');
