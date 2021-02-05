<?php
error_reporting(0);
include 'includes/config.php';

?>

<!DOCTYPE html>

<html lang="en">
<head>
	<meta content="text/html; charset=utf-8" http-equiv="content-type">
	<meta charset="utf-8">
	<meta content="IE=edge" http-equiv="X-UA-Compatible">
	<meta content="" name="description">
	<meta content="" name="keywords">
	<script src="js/jquery.min.js" type="text/javascript">
	</script>

	<title>Mobius Underground - Download</title>
	<link href="css/style.css" rel="stylesheet">
	<link href="images/loader.png" rel="icon" type="image/png">
	<script src='https://www.google.com/recaptcha/api.js'>
	</script>
	<link href='https://fonts.googleapis.com/css?family=Lato:300,400,500' rel='stylesheet' type='text/css'>
	<script src="js/bootstrap.min.js">
	</script>
	<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">
</head>

<body>
	<div id="preloader">
		<div id="preloader-image">
		</div>
	</div>


	<div class="p-anim">
	</div>
	<video playsinline autoplay muted loop id="bgvid">
		<source src="media/bg.webm" type="video/webm">
		<source src="media/bg.mp4" type="video/mp4">
	</video>

	<header>
		<div class="container">
			<div class="header-left">
			</div>
			<div class="header-right">
				<div class="logo">
				</div>
				<div class="menu">
					<a href="index.php">MAIN</a> <a href="dashboard.php">DASHBOARD</a> <a href="#">DONATE</a> <a href="<?php echo $forum;?>">FORUM</a>
				</div>
				<img src="images/logo.png" width="100%">

				<div class="statuses">
					<br>
					<div class="register">
						<a href="<?php echo $dlClient;?>" target="_BLANK" type="button">DOWNLOAD CLIENT</a>
					</div>
					<div class="register">
						<a href="<?php echo $dlPatch;?>" target="_BLANK" type="button">DOWNLOAD PATCH</a>
					</div>
					<div class="register">
						<a href="index.php" type="button">CREATE AN ACCOUNT</a>
					</div>
					<div class="messages">
						</div>
				</div>
			</div>
		</div>
	</header>


	<div class="footer">
		<a href="http://l2jmobius.org"><img alt="" src="images/l2jmobius.png" title=""></a>
	</div>
	<script>
	               var url = 'download.php';
	</script> 
	<script src="js/jquery.cookie.min.js">
	</script> 
	<script src="js/scripts.js">
	</script> 
	<script src="js/validator.js">
	</script> 
	<script src="js/register.js">
	</script>
<!--[if lt IE 9]>
<script src="//oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
<script src="//oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
<![endif]-->
</body>
</html>
