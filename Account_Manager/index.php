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

	<title>Mobius Underground - Main</title>
	<link href="css/register.css" rel="stylesheet">
	<link href="images/loader.png" rel="icon" type="image/png">
	<script src='https://www.google.com/recaptcha/api.js'>
	</script>
	<link href='https://fonts.googleapis.com/css?family=Lato:300,400,500' rel='stylesheet' type='text/css'>
	<script src="js/bootstrap.min.js">
	</script>
	<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">
	<script language="javascript">
	                   $(document).ready(function(){


	                    $('#register').submit(function() {
	                      
	                    if($('#password').val() != $('#passwordVerify').val()){
	                       alert("Please re-enter confirm password");
	                       $('#passwordVerify').val('');
	                       $('#passwordVerify').focus();
	                       return false;
	                    }

	                        function clear_form()
	                        {
	                           $("#email").val('');
	                           $("#username").val('');
	                           $("#password").val('');
	                           $("#passwordVerify").val('');
	                        }
	                   });
	                });
	</script>
</head>

<body>
	<div id="preloader">
		<div id="preloader-image">
		</div>
	</div>


	<div class="p-anim">
	</div>
	<video autoplay="" id="bgvid" loop=""><source src="media/bg.webm" type="video/webm"> <source src="media/bg.mp4" type="video/mp4"></video>

	<header>
		<div class="container">
			<div class="header-left">
			</div>


			<div class="header-right">
				<div class="logo">
				</div>


				<div class="menu">
					<a href="#">MAIN</a> <a href="#">DOWNLOAD</a> <a href="#">DONATE</a> <a href="#">FORUM</a>
				</div>
				<br>
				<img src="images/logo.png" width="100%">

				<div class="statuses">
					<div class="entercp">
						Login to your <a href="ucp">Account</a> or
					</div>
					<br>


					<div class="register">
						<a data-target="#register" data-toggle="modal" type="button">CREATE AN ACCOUNT</a>
					</div>
				</div>
			</div>
		</div>
	</header>


	<div class="modal fade" id="register" role="dialog">
		<div class="container">
			<br>
			<!-- Modal content-->


			<div class="form">
				<div class="modal-body">
					<div class="modal-header">
						<button class="close" data-dismiss="modal" type="button">&times;</button>

						<h4 class="modal-title">Register Account</h4>
					</div>


					<div>
						<form>
							<div class="form-group">
								<input class="form-control" data-error="Account name is required." id="username" name="username" placeholder="Please enter your Account" required="required" type="text" value="<?php if(isset($_POST['username'])) echo $_POST['username'] ?>">

								<div class="help-block with-errors">
								</div>
							</div>


							<div class="form-group">
								<input class="form-control" data-error="Valid email is required." id="email" name="email" placeholder="Please enter your Email" required="required" type="email" value="<?php if(isset($_POST['email'])) echo $_POST['email'] ?>">

								<div class="help-block with-errors">
								</div>
							</div>


							<div class="form-group">
								<input class="form-control" data-error="Password is required." id="password" name="password" placeholder="Please enter your Password" required="required" type="password" value="<?php if(isset($_POST['password'])) echo $_POST['password'] ?>">

								<div class="help-block with-errors">
								</div>
							</div>


							<div class="form-group">
								<input class="form-control" data-error="Verify Password is required." id="passwordVerify" name="passwordVerify" placeholder="Please re-enter your Password" required="required" type="password" value="">

								<div class="help-block with-errors">
								</div>
							</div>
						</form>
						<input class="form-btn btn" name="register" type="submit" value="REGISTER">
					</div>


					<div class="modal-footer">
						<div class="messages">
							<h4><font color="#FFFFFF"><?php
							                                                                                                if(isset($errMsg)){
							                                                                                                    echo $errMsg;
							                                                                                                }
							                                                                                                ?></font>
							</h4>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>


	<div class="footer">
		<a href="http://l2jmobius.com"><img alt="" src="images/l2jmobius.png" title=""></a>
	</div>
	<script>
	               var url = 'index.html';
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
