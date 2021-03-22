terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 3.33.0"
    }
    grafana = {
      source  = "grafana/grafana"
      version = "~> 1.8"
    }
  }
  required_version = "= 0.14.7"
}
