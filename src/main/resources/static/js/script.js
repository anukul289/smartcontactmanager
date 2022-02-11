function toggleSidebar(){
	
	if($(".sidebar").is(":visible")){
		$(".sidebar").css("display","none");
		$(".content").css("margin-left","1%");
	}
	else{
		$(".sidebar").css("display","block");
		$(".content").css("margin-left","20%");
	}
};


function deleteContact(cId)
 {
  		swal({
  	  	  title: "Are you sure?",
  	  	  text: "Once deleted, you will not be able to recover this contact!",
  	  	  icon: "warning",
  	  	  buttons: true,
  	  	  dangerMode: true,
  	  	})
  	  	.then((willDelete) => {
  	  	  if (willDelete) {	
			window.location="/user/delete/"+cId;
			
  	  	  } else {
  	  	    swal("Your contact is safe!");
  	  	  }
  	  	});
 };


