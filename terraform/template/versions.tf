terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "3.59.0"
    }
    grafana = {
      source  = "grafana/grafana"
      version = "1.13.4"
    }
  }

  required_version = "0.14.7"
}
