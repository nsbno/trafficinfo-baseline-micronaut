terraform {
  required_providers {
    aws = {
      source = "hashicorp/aws"
    }
    grafana = {
      source = "grafana/grafana"
    }
  }
  required_version = ">= 0.13"
}
