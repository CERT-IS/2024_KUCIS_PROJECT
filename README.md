# 2024_KUCIS_PROJECT

### 개요

OpenSearch로 로그 데이터를 수집해 보안 이벤트 검출하는 SIEM 프로젝트

- 목표 : 고객이 쉽고 편리하게 적용할 수 있는 클라우드 보안 제품
- 유형 : IaaS
- 동기 : ISMS-P 2.11 인증 심사를 위한 프로세스를 더 편리하고 적은 비용으로 해결할 수 있는 클라우드 SIEM 제품을 개발하고자 함
- 고객층 : 현실적인 이유로 서비스의 보안에 신경쓰기 어려운 대학생이나 소규모 스타트업
- 인원 : 6명

2024 KUCIS 프로젝트로 제출했고, 사이버 시큐리티 해커톤 대회에 참여하고 있습니다.

### 아키텍처 구성

[]

기술 스택 : Spring Webflux, Spring Cloud AWS , OpenSearch, Thymeleaf, OpenHTMLtoPDF, PDFBox

### 상세 설명

직접 AWS의 사용자 그룹을 나눠서 필요한 정책만 허용해서 클라우드상에서 보안 취약점을 최소화하는 협업 구조를 이해할 수 있었습니다.

보안 이벤트 검출에 필요한 로그 데이터`(VPC FlowLogs, ALB AccessLog, CloudTrail Logs, WAF Log)`를 CloudWatch Logs로 수집할 수 있도록 Lambda 함수를 사용했습니다.

ELK 스택을 활용해서 로그 데이터들을 OpenSearch Service에 구독했고, 대시보드 웹서비스를 개발하여 시각화 및 이벤트 검출을 진행했습니다.

기본적으로 AWS의 GarudDuty와 WAF 서비스의 검출 로직을 따르지만, AWS의 보안 서비스는 취약점 검출에 있어서 어떤 비즈니스 로직을 가지고 판별하는지 공개하지 않고 있습니다.

그래서 본 프로젝트에서는 AWS의 보안 서비스(WAF)와 함께 추가적으로 **수집한 로그 데이터를 토대로 취약점을 직접 검출해내는 로직**을 구현하고 있습니다.

웹 서비스의 보안 취약점과 클라우드 콘솔로 접근하는 해킹 시도로 나눠서 진행중입니다.

직접 시뮬레이션을 진행하면서 취약점을 찾고, 로그 데이터를 토대로 정규 표현식으로 정리해 검출하고 있습니다.

제품 대시보드를 제공해서 실시간으로 검출과 모니터링을 진행할 수 있게 제공하고 있습니다.

### 취약점 직접 검출을 위한 모의 시뮬레이션 주입

‘주요 정보통신기반 시설의 기술적 취약점 분석 평가 방법 상세 가이드’를 준수하여 웹 기반 서비스의 기술 취약점 항목을 우선적으로 검출하도록 구현하고 있습니다.

클라우드상에 배포되는 웹서비스의 취약점들을 검출해 데이터를 수집할 수 있도록 모의 시뮬레이션용 웹서비스를 직접 구현

기본적으로 aws에서 제공하는 WAF 보안 취약점을 검출합니다.

그 외에 현재 직접 검출 가능한 취약점 :

- CloudTrail 로그를 통해서 특정 리젼 이외의 국가에서 클라우드 콘솔 접속 시도 검출
- CloudTrail 로그를 통해서 클라우드 콘솔의 비정상적인 계정 접속 시도 검출
- ALB Access Logs를 통해서 쿼리 패러미터를 통해 접근하는 SQL Injection, XSS 시도 검출

### ISMS-P 2.11 침해 대응 보고서 자동화 및 커스텀 형식 제공

검출되는 취약점들에 대해서 피해 규모를 상정하고 상세 데이터들을 정리해서 isms-p 2.11 침해 대응에 필요한 규격의 보고서를 작성합니다.

pdf로 미리보기와 다운로드가 가능하며, 기본적으로 제공되는 보고서 양식 이외에도 커스텀하여 설정할 수 있습니다.

html로 작성된 파일 양식을 통해 고객이 직접 원하는 형식에 맞춰서 보고서 형태를 변경
</br></br></br>
<진행 예정인 내용>

- Kubernetes 환경에서 Service로 진행할 수 있도록 Helm 레포지토리 제공
- Terraform을 이용해 로그 데이터를 수집할 AWS 리소스를 개발자에게 친숙한 코드로 생성 (IaC)
- 클라우드 비용 절감을 위한 시도