terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
      version = "~> 3.21.0"
    }
    grafana = {
      source = "grafana/grafana"
      version = "~> 1.8"
    }
  }
}
