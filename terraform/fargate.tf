
data "aws_caller_identity" "current" {}

resource "aws_ecs_task_definition" "ecs_task_definition" {
  family                = "fargate-task"
  execution_role_arn = aws_iam_role.ecs_task_exec_role.arn
  network_mode = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu = 256
  memory = 512

  task_role_arn = aws_iam_role.ecs_task_role.arn
  container_definitions = jsonencode([
    {
      name = "zapp-spring-api"
      image = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.region}.amazonaws.com/${var.ecr_repo_name}:latest"
      portMappings = [
        {
          containerPort = 8080
          protocol = "tcp"
        }
      ]
      environment: [
        {
          "name": "DB_URL",
          "value": "${data.aws_db_instance.rds_data.endpoint}:5432/${data.aws_db_instance.rds_data.db_name}"
        },
        {
          "name": "DB_USERNAME",
          "value": local.db_creds.username
        },
        {
          "name": "DB_USERNAME",
          "value": local.db_creds.password
        },

      ],
      secrets: [
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group = aws_cloudwatch_log_group.ecs_logs.name
          awslogs-create-group = "true"
          awslogs-region = var.region
          awslogs-stream-prefix = "ecs"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "ecs_service" {
  name = "zapp-api-service"
  cluster = aws_ecs_cluster.ecs_cluster.id
  task_definition = aws_ecs_task_definition.ecs_task_definition.arn
  desired_count = 1
  launch_type = "FARGATE"

  network_configuration {
    subnets = [aws_subnet.public-subnet.id]
    security_groups = [aws_security_group.fargate_sg.id]
    assign_public_ip = true
  }

  force_new_deployment = true

}