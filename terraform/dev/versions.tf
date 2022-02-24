terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.63.0"
    }
    grafana = {
      source  = "grafana/grafana"
      version = "1.20.1"
    }
  }
  required_version = "1.0.0"
}
